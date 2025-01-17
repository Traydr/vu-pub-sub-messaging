package shared.models.communication;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.BufferOverflowException;
import java.nio.channels.SocketChannel;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import shared.Config;
import shared.util.Styling;


public class TransmissionBuffer {

    private final int minSize;

    private ByteBuffer in;
    private ByteBuffer out;

    private TransmissionBuffer(int size) {
        minSize = Math.max(size, Config.MIN_TRANS_BUFFER_SIZE);
        in = ByteBuffer.allocate(minSize);
        out = ByteBuffer.allocate(minSize);
        in.mark();
    }

    public static TransmissionBuffer allocate(int capacity) {
        return new TransmissionBuffer(capacity);
    }

    public static TransmissionBuffer allocate() {
        return allocate(0);
    }

    private synchronized void ensureRemainingReadSpace() {
        //TODO: Fix buffer submitting before actual data can be read due to non-growing size
        //Styling.printError("Before: " + in.capacity());
        if (in == null) {
            in = ByteBuffer.allocate(minSize);
            in.mark();
        } else if (in.remaining() == 0) {
            var pos = in.position();
            in.reset();
            var mark = in.position();
            in.position(pos);
            in.flip();
            //Styling.printError("During: " + in.capacity());
            //Styling.printError("Left: " + in.remaining());
            in = ByteBuffer.allocate(in.capacity() * 2).put(in);
            in.position(mark);
            in.mark();
            in.position(pos);
        }
        //Styling.printError("After: " + in.capacity());
        //Styling.printError("ALeft: " + in.remaining());
    }

    public synchronized boolean read(SocketChannel channel) throws IOException {
        ensureRemainingReadSpace();
        var bytes = channel.read(in);
        if (bytes == -1)
            throw new SocketException("Connection closed prematurely");
        return bytes > 0;
    }

    public synchronized boolean write(SocketChannel channel) throws IOException {
        var pos = out.position();
        out.flip();
        channel.write(out);
        pos -= out.position();
        if (!out.hasRemaining()) {
            out.position(0);
            return false;
        }
        out.limit(out.capacity() - 1);
        out.compact();
        out.position(pos);
        return true;
    }

    private byte[] getNonZeroBytes(byte[] data) {
        List<Byte> result = new ArrayList<>(data.length);
        for (int i = 0; i < data.length; i++) {
            if (data[i] != 0) {
                result.add(data[i]);
            }
        }
        byte[] resultArray = new byte[result.size()];
        for (int i = 0; i < result.size(); i++) {
            resultArray[i] = result.get(i);
        }
        return resultArray;
    }

    public synchronized ArrayList<Object> retrieveObjects() {
        //TODO: Fix buffer submitting before actual data can be read due to non-growing size
//        StringBuilder hexString = new StringBuilder();
//        for (byte b : in.array()) {
//            String hex = Integer.toHexString(0xff & b);
//            if (hex.length() == 1) hexString.append('0');
//            hexString.append(hex);
//        }
//        Styling.printError(hexString.toString());
        //var bytes = getNonZeroBytes(in.slice(0, in.position()).array());
        //if (bytes.length > 5)
        //    Styling.printError(new String(bytes));
        var objects = new ArrayList<>();
        var lastPos = in.position();
        in.reset();
        while (in.position() < lastPos)
            if (in.get() == '\f') {
                try (var bis = new ByteArrayInputStream(in.slice(0, in.position() - 1).array());
                     var ois = new ObjectInputStream(bis)) {
                    for (;;)
                        objects.add(ois.readObject());
                } catch (IOException | ClassNotFoundException ignored) {
                    //Styling.printError(ignored.getMessage());
                }
                lastPos -= in.position();
                in.compact();
                in.position(0);
            }
        in.mark();
        return objects;
    }

    public synchronized void storeObject(Object item) {
        //Styling.printError("TransmissionBuffer: storeObject -> " + item.toString());
        try (var bos = new ByteArrayOutputStream();
             var oos = new ObjectOutputStream(bos)) {
            oos.writeObject(item);
            oos.flush();
            bos.write('\f');
            var data = bos.toByteArray();
            if (out == null) out = ByteBuffer.allocate(minSize);
            for (;;)
                try {
                    out.put(data);
                    break;
                } catch (BufferOverflowException e) {
                    out.flip();
                    out = ByteBuffer.allocate(out.capacity() * 2).put(out);
                }
        } catch (IOException ignored) {
            //Styling.printError("TransmissionBuffer: error -> " + ignored.getMessage().toString());
        }
    }

}