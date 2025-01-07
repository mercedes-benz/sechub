// SPDX-License-Identifier: MIT
// This is implemented according to the sechub default client in Java
// Instead of Multipart (Java) we use FromData (TypeScript)
type FormDataContentType = 'STRING' | 'FILE' | 'STREAM' | 'BOUNDARY';

interface FormDataContent {
  name: string;
  type: FormDataContentType;
  value?: string;
  file?: File;
  stream?: () => Promise<ArrayBuffer>;
  filename?: string;
  contentType?: string;
}

export class FormDataBodyBuilder {
  private formDataContent: FormDataContent[] = []
  private boundary: string

  constructor (boundary: string) {
    this.boundary = boundary
  }

  public async build (): Promise<Blob> {
    if (this.formDataContent.length === 0) {
      throw new Error('Must have at least one part to build a formData message.')
    }

    this.formDataContent.push(this.boundaryPart())

    const parts: (string | Uint8Array)[] = []

    for (const part of this.formDataContent) {
      parts.push(this.constructPartHeader(part))

      if (part.type === 'FILE' && part.file) {
        await this.appendFileInChunks(part.file, parts)
      } else if (part.type === 'STREAM' && part.stream) {
        parts.push(new Uint8Array(await part.stream()))
      }

      if (part.type !== 'BOUNDARY') {
        parts.push(new TextEncoder().encode('\r\n'))
      }
    }
    return new Blob(parts)
  }

  public addString (name: string, value: string): this {
    this.formDataContent.push({ name, type: 'STRING', value })
    return this
  }

  public addFile (name: string, file: File): this {
    this.formDataContent.push({ name, type: 'FILE', file, contentType: file.type })
    return this
  }

  public addStream (name: string, stream: () => Promise<ArrayBuffer>, filename: string, contentType: string): this {
    this.formDataContent.push({ name, type: 'STREAM', stream, filename, contentType })
    return this
  }

  private boundaryPart (): FormDataContent {
    return { name: '', type: 'BOUNDARY', value: `--${this.boundary}--` }
  }

  private constructPartHeader (part: FormDataContent): Uint8Array {
    if (part.type === 'STRING') {
      return new TextEncoder().encode(
        `--${this.boundary}\r\nContent-Disposition: form-data; name="${part.name}"\r\nContent-Type: text/plain; charset=UTF-8\r\n\r\n${part.value}`
      )
    } else if (part.type === 'BOUNDARY') {
      return new TextEncoder().encode(`--${this.boundary}--\r\n`)
    } else {
      const filename = part.file ? part.file.name : part.filename
      const contentType = part.contentType || 'application/octet-stream'
      return new TextEncoder().encode(
        `--${this.boundary}\r\nContent-Disposition: form-data; name="${part.name}"; filename="${filename}"\r\nContent-Type: ${contentType}\r\n\r\n`
      )
    }
  }

  private async appendFileInChunks (file: File, parts: (string | Uint8Array)[]): Promise<void> {
    // const chunkSize = 64 * 1024 * 1024 // 64MB chunks
    const chunkSize = 8192; // 8KB chunks
    for (let offset = 0; offset < file.size; offset += chunkSize) {
      const chunk = file.slice(offset, offset + chunkSize)
      const arrayBuffer = await chunk.arrayBuffer()
      parts.push(new Uint8Array(arrayBuffer))
    }
  }
}
