package ir.amv.os.intellij.plugins.cold.swap.tools;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;

public class ExceptionStackTraceWriter {

    public static void printStackTrace(Throwable throwable, Consumer<String> stringConsumer) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer, true));
        stringConsumer.accept(writer.getBuffer().toString());
    }
}
