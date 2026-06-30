import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Run extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(Run.class.getName());
    private static final String DEFAULT_JAVA_COMMAND = "java";
    private static final String UTF_8 = "UTF-8";

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding(UTF_8);
        response.setCharacterEncoding(UTF_8);
        response.setContentType("text/plain; charset=UTF-8");

        PrintWriter writer = response.getWriter();
        String className = trimToEmpty(request.getParameter("className"));

        if (!isValidClassName(className)) {
            writer.println("Please enter a valid Java class name.");
            return;
        }

        File applicationRoot = new File(getServletContext().getRealPath("/"));
        File classesDirectory = new File(new File(applicationRoot, "Files"), "classes");
        File classFile = new File(classesDirectory, className + ".class");

        if (!classFile.exists()) {
            writer.println("Compiled class was not found. Please compile the program first.");
            return;
        }

        ProcessResult result = runProcess(new String[] {
            getJavaCommand(),
            "-cp",
            classesDirectory.getAbsolutePath(),
            className
        });

        if (result.output.length() > 0) {
            writer.print(result.output);
        }
        if (result.error.length() > 0) {
            writer.print(result.error);
        }
        if (result.output.length() == 0 && result.error.length() == 0 && result.exitCode != 0) {
            writer.println("Program failed with exit code " + result.exitCode + ".");
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain; charset=UTF-8");
        response.getWriter().println("Run requests must use POST.");
    }

    private String getJavaCommand() {
        String configuredCommand = getInitParameter("javaCommand");
        if (configuredCommand == null || configuredCommand.trim().length() == 0) {
            return DEFAULT_JAVA_COMMAND;
        }
        return configuredCommand.trim();
    }

    private ProcessResult runProcess(String[] command) throws IOException {
        Process process = null;
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            process = builder.start();

            StreamCollector outputCollector = new StreamCollector(process.getInputStream());
            StreamCollector errorCollector = new StreamCollector(process.getErrorStream());
            outputCollector.start();
            errorCollector.start();

            int exitCode = process.waitFor();
            outputCollector.join();
            errorCollector.join();

            return new ProcessResult(exitCode, outputCollector.getContent(), errorCollector.getContent());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Program execution was interrupted.", ex);
            return new ProcessResult(1, "", "Program execution was interrupted. Please try again.");
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Unable to start java process.", ex);
            throw ex;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static boolean isValidClassName(String className) {
        return className.matches("[A-Za-z_$][A-Za-z0-9_$]*");
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static class StreamCollector extends Thread {
        private final InputStream inputStream;
        private final StringBuffer content = new StringBuffer();

        StreamCollector(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public void run() {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append('\n');
                }
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Unable to read process output.", ex);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        LOGGER.log(Level.WARNING, "Unable to close process output reader.", ex);
                    }
                }
            }
        }

        String getContent() {
            return content.toString();
        }
    }

    private static class ProcessResult {
        final int exitCode;
        final String output;
        final String error;

        ProcessResult(int exitCode, String output, String error) {
            this.exitCode = exitCode;
            this.output = output;
            this.error = error;
        }
    }
}
