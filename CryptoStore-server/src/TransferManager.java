import javax.net.ssl.SSLSocket;
import java.io.*;
import java.nio.ByteBuffer;

public class TransferManager {
    private DataInputStream dis;
    private DataOutputStream dos;

    public TransferManager(SSLSocket socket) throws Exception {
        try {
            dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        } catch (IOException e) {
            throw new Exception(Error.CANNOT_CONNECT.getDescription());
        }
    }

    public void writeControl(Command command) throws Exception {
        try {
            byte[] buffer = new byte[1];
            buffer[0] = (byte) command.getCode();

            Control controlPkts = new Control(buffer);

            write(controlPkts);

        } catch (IOException e) {
            throw new Exception(Error.FAILED_TO_WRITE.getDescription());
        }
    }

    public void writeFileSize(int size) throws Exception {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            //buffer.putLong(0, size);
            buffer.putInt(size);
            //byte[] buffer = new byte[Integer.BYTES];
            //buffer[0] = (byte) size;

            FileSize dataPkts = new FileSize(buffer.array());

            write(dataPkts);

        } catch (IOException e) {
            throw new Exception(Error.FAILED_TO_WRITE.getDescription());
        }
    }

    public void write(Packets pkts) throws Exception {
        try {
            dos.write(pkts.getData(0));
            dos.flush();
        } catch (IOException e) {
            throw new Exception(Error.FAILED_TO_WRITE.getDescription());
        }
    }

    public Packets read(long sizeOfFile) throws Exception {
        try {
            char type = (char) (dis.readByte() & 0xFF);
            byte[] load;

            switch (type) {
                case 'C':
                    load = new byte[1];
                    dis.read(load);
                    return new Control(load);

                case 'N':
                    load = new byte[(int) sizeOfFile];
                    dis.read(load);
                    return new Filename(load);

                case 'S':
                    load = new byte[Long.BYTES];
                    dis.read(load);
                    return new FileSize(load);

                case 'F':
                    if (sizeOfFile > 0) {
                        int pos = 0;
                        int bytesRead;
                        load = new byte[(int) sizeOfFile];
                        byte[] buff = new byte[4096];

                        if (sizeOfFile>4096) {
                            for (int i = 0; i < (int) Math.ceil((double) sizeOfFile / (double) 4096); i++) {
                                bytesRead = dis.read(buff);
                                System.arraycopy(buff, 0, load, pos, bytesRead);
                                pos += bytesRead;
                                System.out.println(pos);
                            }
                        }
                        bytesRead = dis.read(buff);
                        System.arraycopy(buff, 0,load, pos, (int)(sizeOfFile%4096));

                        return new FileData(load);

                    } else {
                        throw new IOException();
                    }

                default:
                    throw new IOException();
            }

        } catch (IOException e) {
            throw new Exception(Error.FAILED_TO_READ.getDescription());
        }
    }

    public void closeStreams() throws Exception {
        try {
            dis.close();
            dos.close();
        } catch (IOException e) {
            throw new Exception(Error.FAILED_TO_CLOSE_STREAMS.getDescription());
        }
    }
}
