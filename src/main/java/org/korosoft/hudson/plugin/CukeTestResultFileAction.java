package org.korosoft.hudson.plugin;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import org.apache.commons.lang.StringEscapeUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Russian Salad Cucumber test result file server action
 *
 * @author Dmitry Korotkov
 * @since 1.0
 */
public class CukeTestResultFileAction implements Action {
    public static final String CUKE_RESULT = "cukeResult";
    private final AbstractBuild<?, ?> build;
    private final FilePath reportFolder;

    public CukeTestResultFileAction(AbstractBuild<?, ?> build, FilePath reportFolder) {
        this.build = build;
        this.reportFolder = reportFolder;
        saveReportFiles(reportFolder);
    }

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return "RSFiles";
    }

    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        String path = req.getRestOfPath();
        path = path.replaceAll("[/\\\\]\\.\\.[/\\\\]", "/").replaceAll("^[\\\\/]*", "");

        FilePath reportPath = new FilePath(build.getRootDir()).child(CUKE_RESULT);
        FilePath serveFile = reportPath.child(path);
        try {
            if (!serveFile.exists() && path.endsWith(".xml")) {
                FilePath srtFile = reportPath.child(path.substring(0, path.length() - 4) + ".srt");
                if (srtFile.exists()) {
                    serveSrtAsTimeText(req, rsp, srtFile);
                }
            }
            if (!serveFile.exists()) {
                rsp.sendError(HttpServletResponse.SC_NOT_FOUND, "File " + req.getRestOfPath() + " not found");
            }
            if (serveFile.isDirectory()) {
                rsp.sendError(HttpServletResponse.SC_FORBIDDEN, "Directory browsing is not enabled");
            }
            rsp.serveFile(req, serveFile.read(), serveFile.lastModified(), serveFile.length(), serveFile.getName());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void serveSrtAsTimeText(StaplerRequest req, StaplerResponse rsp, FilePath srtFile) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        builder.append("<tt xml:lang=\"en\" xmlns=\"http://www.w3.org/2006/10/ttaf1\" xmlns:tts=\"http://www.w3.org/2006/10/ttaf1#styling\">");
        builder.append("<body>");
        builder.append("<div xml:lang=\"en\">");
        BufferedReader reader = new BufferedReader(new InputStreamReader(srtFile.read()));
        for (String l = reader.readLine(); l != null; l = reader.readLine()) {
            if (l.length() == 0) {
                if (reader.readLine() == null) {
                    break;
                }
            }
            String timing = reader.readLine();
            if (timing == null) {
                break;
            }
            String text = reader.readLine();
            if (text == null) {
                break;
            }
            String[] times = timing.split(" --> ");
            if (times.length != 2) {
                continue;
            }
            String[] start_ms = times[0].split(",");
            String[] end_ms = times[1].split(",");
            if (start_ms.length != 2 || end_ms.length != 2) {
                continue;
            }
            String[] start_s = start_ms[0].split(":");
            String[] end_s = end_ms[0].split(":");
            if (start_s.length != 3 || end_s.length != 3) {
                continue;
            }
            text = StringEscapeUtils.escapeHtml(text);
            long start, end;
            try {
                start = Long.parseLong(start_ms[1]) + 1000 * Long.parseLong(start_s[2]) + 60000 * Long.parseLong(start_s[1]) + 3600000 * Long.parseLong(start_s[0]);
                end = Long.parseLong(end_ms[1]) + 1000 * Long.parseLong(end_s[2]) + 60000 * Long.parseLong(end_s[1]) + 3600000 * Long.parseLong(end_s[0]);
            } catch (NumberFormatException e) {
                continue;
            }
            String v1 = Integer.toString(Integer.parseInt(start_ms[1]) / 10);
            if (v1.length() == 1) {
                v1 = "0" + v1;
            }
            String v2 = Long.toString((end - start) / 1000);
            String v3 = Long.toString(((end - start) % 1000) / 10);
            if (v3.length() == 1) {
                v3 = "0" + v3;
            }
            text = text.replaceAll("color=(#[0-9a-fA-F]+)", "color=\"$1\"");
            builder.append(String.format("<p begin=\"%s:%s:%s.%s\" dur=\"%s.%s\">%s</p>", start_s[0], start_s[1], start_s[2], v1, v2, v3, text));

        }
        builder.append("</div></body></tt>");
        reader.close();

        rsp.setContentType("text/xml");
        rsp.setCharacterEncoding("UTF-8");
        PrintWriter writer = rsp.getWriter();
        writer.print(builder.toString());
        writer.close();
    }

    private void saveReportFiles(FilePath reportFolder) {
        final File cukeResult = new File(build.getRootDir(), CUKE_RESULT);
        if (!cukeResult.mkdir()) {
            throw new RuntimeException("Failed to create folder " + cukeResult);
        }
        try {
            reportFolder.copyRecursiveTo(new FilePath(cukeResult));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
