// SPDX-License-Identifier: MIT
import CryptoJS from 'crypto-js'

export async function createSha256Checksum (file: File): Promise<string> {
  const chunkSize = 64 * 1024 * 1024 // 64MB chunks
  const fileReader = new FileReader()
  let offset = 0
  const sha256 = CryptoJS.algo.SHA256.create()

  return new Promise<string>((resolve, reject) => {
    fileReader.onload = (event: ProgressEvent<FileReader>) => {
      if (!event.target) {
        reject(new Error('FileReader event target is null'))
        return
      }

      const data = event.target.result as ArrayBuffer
      const wordArray = CryptoJS.lib.WordArray.create(data)
      sha256.update(wordArray)

      offset += chunkSize
      if (offset < file.size) {
        readNextChunk()
      } else {
        const hash = sha256.finalize()
        resolve(hash.toString(CryptoJS.enc.Hex))
      }
    }

    fileReader.onerror = error => {
      reject(error)
    }

    function readNextChunk () {
      const slice = file.slice(offset, offset + chunkSize)
      fileReader.readAsArrayBuffer(slice)
    }

    readNextChunk()
  })
}
