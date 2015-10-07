import javax.net.ssl.SSLSocket;
import java.io.*;

public class TransferManager {
    private DataInputStream dis;
    private DataOutputStream dos;

    public TransferManager(SSLSocket socket) {
        try {
            dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        } catch (IOException e) {
            Error.CANNOT_CONNECT.print();
        }
    }

    public void writeControl(Command command) {
        byte[] buffer = new byte[1];
        buffer[0] = (byte) command.getCode();

        Control controlPkts = new Control(buffer);

        write(controlPkts);
    }

    public void writeFileSize(int size) { //TODO change that to long
        //ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        //buffer.putLong(0, size);
        byte[] buffer = new byte[1];
        buffer[0] = (byte) size;

        FileSize dataPkts = new FileSize(buffer);

        write(dataPkts);
    }

    public void write(Packets pkts) {
        try {
            dos.write(pkts.getData(0));
            dos.flush();
        } catch (IOException e) {
            Error.PACKETS_NOT_SENT.print();
        }
    }

    public Packets read(long sizeOfFile) throws IOException {
        char type = (char) (dis.readByte() & 0xFF);
        byte[] load;

        switch (type) {
            case 'C':
                load = new byte[1];
                dis.read(load);
                return new Control(load);

            case 'N':
                load = new byte[(int) sizeOfFile]; //TODO this is not right need fixing
                dis.read(load);
                return new Filename(load);

            case 'S':
                load = new byte[Long.BYTES];
                dis.read(load);
                return new FileSize(load);

            case 'F':
                if (sizeOfFile > 0) {
                    load = new byte[(int) sizeOfFile]; //TODO fix
                    dis.read(load);
                    return new FileData(load);

                } else {
                    throw new IOException("Could not read from socket");
                }

            default:
                Error.CANNOT_READ.print();
                throw new IOException("Packet type doesn't exist");
        }
    }

    public void closeStreams() throws IOException {
        dis.close();
        dos.close();
    }
}
