package net.sn0wix_.gui;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

public class MultiplePrintStreams extends PrintStream {
    private final PrintStream[] streams;

    public MultiplePrintStreams(PrintStream... streams) {
        super(new OutputStream() {

            @Override
            public void write(int b) {

            }
        });

        this.streams = streams;
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        Arrays.stream(streams).forEach(printStream -> printStream.write(buf, off, len));
    }

    @Override
    public void flush() {
        Arrays.stream(streams).forEach(PrintStream::flush);
    }
}
