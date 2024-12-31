// SPDX-License-Identifier: MIT
// This is implemented according to the sechub default client in Java
export type MultiPartType = 'STRING' | 'FILE' | 'STREAM' | 'BOUNDARY';

export interface MultiPartData {
  name: string;
  type: MultiPartType;
  value?: string;
  file?: File;
  stream?: () => Promise<ArrayBuffer>;
  filename?: string;
  contentType?: string;
}

export class MultiPartBodyPublisherBuilder {
  private multiPartData: MultiPartData[] = []
  private boundary: string

  constructor (boundary: string) {
    this.boundary = boundary
  }

  public async build (): Promise<Blob> {
    if (this.multiPartData.length === 0) {
      throw new Error('Must have at least one part to build a multipart message.')
    }

    this.multiPartData.push(this.boundaryPart())

    const parts: (string | Uint8Array)[] = []

    for (const part of this.multiPartData) {
      parts.push(this.constructPartHeader(part))

      if (part.type === 'FILE' && part.file) {
        parts.push(new Uint8Array(await part.file.arrayBuffer()))
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
    this.multiPartData.push({ name, type: 'STRING', value })
    return this
  }

  public addFile (name: string, file: File): this {
    this.multiPartData.push({ name, type: 'FILE', file, contentType: file.type })
    return this
  }

  public addStream (name: string, stream: () => Promise<ArrayBuffer>, filename: string, contentType: string): this {
    this.multiPartData.push({ name, type: 'STREAM', stream, filename, contentType })
    return this
  }

  private boundaryPart (): MultiPartData {
    return { name: '', type: 'BOUNDARY', value: `--${this.boundary}--` }
  }

  private constructPartHeader (part: MultiPartData): Uint8Array {
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
}
