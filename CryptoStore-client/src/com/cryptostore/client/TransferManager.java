package com.cryptostore.client;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.lang.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class TransferManager {
    private static final int BLOCK_SIZE = 4096;
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

    public void flush() {
        try {
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeFileName(String filename) throws Exception {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(filename.getBytes().length);
            buffer.put(filename.getBytes());

            Filename dataPkts = new Filename(buffer.array());

            dos.write(dataPkts.getData(0));
            dos.flush();

        } catch (IOException e) {
            throw new Exception(Error.FAILED_TO_WRITE.getDescription());
        }
    }

    public void writeControl(Command command) throws Exception {
        try {
            byte[] buffer = new byte[1];
            buffer[0] = (byte) command.getCode();

            Control controlPkts = new Control(buffer);

            dos.write(controlPkts.getData(0));
            dos.flush();

        } catch (IOException e) {
            throw new Exception(Error.FAILED_TO_WRITE.getDescription());
        }
    }

    public void writeFileSize(int size) throws Exception {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.putInt(size);

            FileSize dataPkts = new FileSize(buffer.array());

            dos.write(dataPkts.getData(0));
            dos.flush();

        } catch (IOException e) {
            throw new Exception(Error.FAILED_TO_WRITE.getDescription());
        }
    }

    public void writeFile(Packets pkts) throws Exception {
        try {
            byte[] fullSizedFile = pkts.getData(1);
            byte[][] chunks = divideArray(fullSizedFile, BLOCK_SIZE);

            dos.write(pkts.getData(0)[0]); //send type separately
            for (int i=0; i<chunks.length; i++){
                dos.write(chunks[i]);
                dos.flush();
            }

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
                        byte[] buff = new byte[BLOCK_SIZE];

                        if (sizeOfFile>BLOCK_SIZE) {
                            System.out.println((int) Math.ceil((double) sizeOfFile / (double) BLOCK_SIZE));
                            for (int i = 0; i < ((int) Math.ceil((double) sizeOfFile / (double) BLOCK_SIZE))-1; i++) {
                                bytesRead = dis.read(buff);
                                System.arraycopy(buff, 0, load, pos, bytesRead);
                                pos += bytesRead;
                            }
                        }
                        dis.read(buff);
                        if ((sizeOfFile%BLOCK_SIZE)!=0) {
                            System.arraycopy(buff, 0, load, pos, (int) sizeOfFile-pos);
                        } else {
                            System.arraycopy(buff, 0, load, pos, BLOCK_SIZE);
                        }
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

    private byte[][] divideArray(byte[] source, int chunkSize) {
        byte[][] chunks = new byte[(int)Math.ceil(source.length / (double)chunkSize)][chunkSize];
        int srcPos = 0;

        for(int i = 0; i < chunks.length; i++) {
            chunks[i] = Arrays.copyOfRange(source, srcPos, srcPos + chunkSize);
            srcPos += chunkSize;
        }

        return chunks;
    }
}
