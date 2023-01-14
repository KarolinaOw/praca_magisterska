import WebHDFS from 'webhdfs';
import tmp from 'tmp';
import * as fs from "fs";
import * as uuid from "uuid";

const hdfs = WebHDFS.createClient();

export function uploadRaw(data: any) {
    let tmpFile = tmp.fileSync();
    fs.writeFileSync(tmpFile.name, data);

    const localFileStream = fs.createReadStream(tmpFile.name);

    const id = uuid.v4()
    const remoteFileStream = hdfs.createWriteStream(`/logo/input-data/${id}.input`);

    localFileStream.pipe(remoteFileStream);

    remoteFileStream.on('error', (err: any) => {
        console.log(err);
    });
    remoteFileStream.on('finish', () => {
        console.log(`File ${remoteFileStream.name} uploaded`);
    });
}

function uploadFile() {
    var localFileStream = fs.createReadStream('/path/to/local/file');
    var remoteFileStream = hdfs.createWriteStream('/path/to/remote/file');
}