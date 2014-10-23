package me.FurH.CreativeControl.core.configuration;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class ConfigUpdater
{
    private Pattern space_regex;
    private Pattern skip_chars;
    private Pattern invalid_start;
    
    public ConfigUpdater() {
        super();
        this.space_regex = Pattern.compile(" ([ ])");
        this.skip_chars = Pattern.compile("[^\\x00-\\x7E]");
        this.invalid_start = Pattern.compile("^[^ a-zA-Z0-9\\w]");
    }
    
    public void updateLines(final File main, final InputStream is) {
        BufferedWriter writer = null;
        OutputStreamWriter osw = null;
        FileOutputStream fos = null;
        try {
            final List<String[]> source = this.getFileLines(is);
            final List<String[]> client = this.getFileLines(main);
            fos = new FileOutputStream(main);
            osw = new OutputStreamWriter(fos, "UTF-8");
            writer = new BufferedWriter(osw);
            final HashSet<String> wroten = new HashSet<String>();
            String lastComment = "";
            final String l = System.getProperty("line.separator");
            for (int j1 = 0; j1 < source.size(); ++j1) {
                final String[] line = source.get(j1);
                final String node = line[0];
                if (node.startsWith("#{S}")) {
                    lastComment = node.substring(4);
                }
                else if (node.isEmpty()) {
                    writer.write(l);
                }
                else if (this.isList(node)) {
                    writer.write(node + l);
                }
                else if (this.isComment(node)) {
                    writer.write(node + l);
                }
                else {
                    boolean isList = false;
                    if (this.isList(node) || node.endsWith(";")) {
                        isList = true;
                    }
                    final String[] sections = node.split("\\.");
                    String spaces = "";
                    for (int index = 0; sections.length > index; ++index) {
                        final boolean lastIndex = sections.length <= index + 1;
                        final String section = sections[index].replaceAll(";", "");
                        if (isList || !lastIndex) {
                            if (wroten.add(spaces + section)) {
                                writer.write(spaces + section + ":" + l);
                            }
                            spaces += "  ";
                        }
                        else if (line.length > 1) {
                            if (!lastComment.isEmpty()) {
                                writer.write(lastComment + l);
                                lastComment = "";
                            }
                            String value = line[1];
                            for (final String[] data : client) {
                                if (data.length > 1 && data[0].equalsIgnoreCase(node)) {
                                    value = data[1];
                                    break;
                                }
                            }
                            writer.write(spaces + section + ": " + this.trateString(value) + l);
                        }
                    }
                }
            }
            writer.write(l + "# End of the file -->");
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
                if (osw != null) {
                    osw.flush();
                    osw.close();
                }
                if (fos != null) {
                    fos.flush();
                    fos.close();
                }
            }
            catch (IOException ex2) {}
        }
    }
    
    private List<String[]> getFileLines(final File file) {
        final FileInputStream fis = null;
        try {
            if (!file.exists()) {
                System.out.println("File does not exists!");
                return null;
            }
            return this.getFileLines(new FileInputStream(file));
        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            }
            catch (IOException ex2) {}
        }
        return null;
    }
    
    private List<String[]> getFileLines(final InputStream is) {
        Scanner scanner = null;
        try {
            scanner = new Scanner(is, "UTF-8");
            final List<String> lines = new ArrayList<String>();
            while (scanner.hasNext()) {
                lines.add(this.parseInitial(scanner.nextLine()));
            }
            final List<String[]> newLines = new ArrayList<String[]>();
            int last = 0;
            int lastListIndex = -1;
            String lastMultiple = "";
            for (int j1 = 0; j1 < lines.size(); ++j1) {
                String line = lines.get(j1).replaceAll("\t", "  ");
                if (!this.isList(line)) {
                    lastListIndex = -1;
                }
                if (!lastMultiple.isEmpty()) {
                    lastMultiple = (line = lastMultiple + this.removeInitial(this.replaceMultiLine(line)));
                    lastMultiple = "";
                }
                if (line.isEmpty()) {
                    newLines.add(new String[] { line });
                }
                else if (line.trim().isEmpty()) {
                    newLines.add(new String[] { line.trim() });
                }
                else {
                    final int index = this.getSectionNumber(line);
                    boolean nextIsSection = false;
                    if (j1 + 1 < lines.size() - 1) {
                        nextIsSection = (this.getSectionNumber(lines.get(j1 + 1)) == 0);
                    }
                    boolean afterIsSection = false;
                    if (j1 - 1 > -1) {
                        afterIsSection = (this.getSectionNumber(lines.get(j1 - 1)) == 0);
                    }
                    if (this.isComment(line) && !line.startsWith("#") && (nextIsSection || afterIsSection)) {
                        line = "#{S}" + line;
                    }
                    if (this.isComment(line)) {
                        newLines.add(new String[] { line });
                    }
                    else if (this.isList(line)) {
                        int nextList = lastListIndex;
                        if (nextList != -1) {
                            final int p1 = line.indexOf(45);
                            if (p1 != -1) {
                                line = line.substring(p1);
                                String spaces = "  ";
                                while (nextList > 0) {
                                    spaces += "  ";
                                    --nextList;
                                }
                                spaces = (line = spaces + line);
                            }
                        }
                        newLines.add(new String[] { line });
                    }
                    else {
                        final List<String> sections = new ArrayList<String>();
                        if (index == 0) {
                            last = j1;
                        }
                        if (!this.isList(line)) {
                            lastListIndex = index;
                        }
                        try {
                            sections.add(this.parseSpaces(this.getSectionSelf(line)));
                        }
                        catch (Exception ex) {
                            newLines.add(new String[] { line });
                            continue;
                        }
                        int subLine = j1;
                        int pass = -1;
                        final HashSet<String> sectionAdded = new HashSet<String>();
                        if (index > 0) {
                            while (subLine >= last) {
                                if (--subLine <= -1) {
                                    break;
                                }
                                final String old = lines.get(subLine);
                                final int newIndex = this.getSectionNumber(old);
                                if (this.isList(old)) {
                                    continue;
                                }
                                if (!this.isSection(old) || newIndex >= index || newIndex == pass || newIndex == index) {
                                    continue;
                                }
                                if (sectionAdded.add(old + newIndex)) {
                                    sections.add(this.trateSection(old));
                                }
                                pass = newIndex;
                            }
                        }
                        Collections.reverse(sections);
                        String build = "";
                        for (final String string : sections) {
                            build += string;
                        }
                        if (build.endsWith(".")) {
                            build = build.substring(0, build.length() - 1);
                        }
                        String content = "";
                        if (!this.isSection(line)) {
                            final int cut = line.indexOf(58, index) + 2;
                            content = line.substring(cut);
                        }
                        if (this.isMultiLine(content)) {
                            lastMultiple += this.replaceMultiLine(line);
                        }
                        else {
                            boolean nextIsList = false;
                            if (j1 + 1 < lines.size() - 1) {
                                nextIsList = this.isList(lines.get(j1 + 1));
                            }
                            if (!this.isList(line)) {
                                lastListIndex = index;
                            }
                            if (nextIsList) {
                                newLines.add(new String[] { build + ";" });
                            }
                            else {
                                if (!this.isList(line)) {
                                    lastListIndex = index;
                                }
                                if (!content.isEmpty()) {
                                    if (this.isNumberOnly(content)) {
                                        newLines.add(new String[] { build, content.trim() });
                                    }
                                    else {
                                        content = content.trim();
                                        if (content.contains(" ") && !content.startsWith("'") && !content.endsWith("'") && !content.startsWith("\"") && !content.endsWith("\"") && this.isInvalidStart(content)) {
                                            content = "\"" + content + "\"";
                                        }
                                        newLines.add(new String[] { build, content });
                                    }
                                    if (!this.isList(line)) {
                                        lastListIndex = index;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return newLines;
        }
        finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (scanner != null) {
                    scanner.close();
                }
            }
            catch (IOException ex2) {}
        }
    }
    
    private String replaceMultiLine(final String line) {
        return line.replaceAll("'", "");
    }
    
    private boolean isMultiLine(final String line) {
        return line.replaceAll(" ", "").startsWith("'") && !line.replaceAll(" ", "").endsWith("'");
    }
    
    private String removeInitial(String line) {
        while (line.charAt(0) == ' ') {
            line = line.substring(1);
        }
        return line;
    }
    
    private String trateSection(final String line) {
        return this.parseSpaces(line.replaceAll(":", "."));
    }
    
    private String trateString(final String line) {
        String newLine = line;
        final Matcher matcher = this.skip_chars.matcher(line);
        while (matcher.find()) {
            newLine = line.replace(matcher.group(), this.toHex(matcher.group()));
        }
        return newLine;
    }
    
    private String toHex(final String line) {
        final String hex = Integer.toHexString(line.charAt(0));
        String prefix;
        if (hex.length() == 1) {
            prefix = "\\u000";
        }
        else if (hex.length() == 2) {
            prefix = "\\u00";
        }
        else if (hex.length() == 3) {
            prefix = "\\u0";
        }
        else {
            prefix = "\\u";
        }
        return prefix + hex;
    }
    
    private String parseSpaces(String line) {
        if (line.endsWith(" ")) {
            line = line.replaceAll(" ", "");
        }
        else {
            line = this.space_regex.matcher(line).replaceAll("");
        }
        return line;
    }
    
    private boolean isNumberOnly(final String content) {
        return content.replaceAll("[^0-9-.]", "").equals(content);
    }
    
    private String getSectionSelf(final String line) throws StringIndexOutOfBoundsException {
        return line.substring(0, line.indexOf(58));
    }
    
    private boolean isSection(final String line) {
        return !this.isList(line) && line.trim().endsWith(":");
    }
    
    private int getSectionNumber(final String line) {
        int section = 0;
        for (String spaces = "  "; line.startsWith(spaces); spaces += "  ", ++section) {}
        return section;
    }
    
    private boolean isList(final String line) {
        return line.replaceAll(" ", "").startsWith("-");
    }
    
    private boolean isComment(final String line) {
        return line.replaceAll(" ", "").startsWith("#");
    }
    
    private String parseInitial(String line) {
        return line = line.replaceAll("0xFFFD", "");
    }
    
    private boolean isInvalidStart(final String content) {
        return this.invalid_start.matcher(content).matches();
    }
}
