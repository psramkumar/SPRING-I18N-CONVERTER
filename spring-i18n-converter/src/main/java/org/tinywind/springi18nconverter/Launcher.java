/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2016 Jeon JaeHyeong
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.tinywind.springi18nconverter;

import org.tinywind.springi18nconverter.jaxb.Configuration;
import org.tinywind.springi18nconverter.jaxb.Source;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.System.exit;

/**
 * @author tinywind
 */
public class Launcher {
    public final static String CONVERTER_XSD = "spring-i18n-converter-0.1.xsd";
    public final static String REPO_XSD_URL = "https://raw.githubusercontent.com/tinywind/SPRING-I18N-CONVERTER/master/spring-i18n-converter/src/main/resources/xsd/";
    public final static String REPO_CONVERTER_XSD = REPO_XSD_URL + CONVERTER_XSD;
    public final static String MESSAGES_FILE_FORMAT = "messages_?([a-zA-Z_]+)?[.]properties";

    private static boolean isCorrected(Configuration configuration) {
        if (configuration.getSources() == null || configuration.getSources().size() == 0)
            return false;
        for (Source source : configuration.getSources())
            if (source.getSourceDir() == null)
                return false;
        return true;
    }

    private static void setDefault(Configuration configuration) throws NoSuchFieldException {
        for (Source source : configuration.getSources()) {
            if (source.getConverter() == null)
                source.setConverter(Source.class.getField("converter").getAnnotation(XmlElement.class).defaultValue());
            if (source.getSourceEncoding() == null)
                source.setSourceEncoding(Source.class.getField("sourceEncoding").getAnnotation(XmlElement.class).defaultValue());
            if (source.getTargetDir() == null)
                source.setTargetDir(Source.class.getField("targetDir").getAnnotation(XmlElement.class).defaultValue());
            if (source.getTargetEncoding() == null)
                source.setTargetEncoding(Source.class.getField("targetEncoding").getAnnotation(XmlElement.class).defaultValue());
            if (source.getExcludes() == null)
                source.setExcludes(Source.class.getField("excludes").getAnnotation(XmlElement.class).defaultValue());
            if (source.isOverwrite() == null)
                source.setOverwrite(Boolean.valueOf(Source.class.getField("overwrite").getAnnotation(XmlElement.class).defaultValue()));
            if (source.isDescribeByUnicode() == null)
                source.setDescribeByUnicode(Boolean.valueOf(Source.class.getField("describeByUnicode").getAnnotation(XmlElement.class).defaultValue()));
            if (source.isToMessageProperties() == null)
                source.setToMessageProperties(Boolean.valueOf(Source.class.getField("toMessageProperties").getAnnotation(XmlElement.class).defaultValue()));
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage : " + Launcher.class.getName() + " <configuration-file>");
            exit(-1);
        }

        for (String arg : args) {
            InputStream in = Launcher.class.getResourceAsStream(arg);
            try {
                if (in == null && !arg.startsWith("/"))
                    in = Launcher.class.getResourceAsStream("/" + arg);

                if (in == null && new File(arg).exists())
                    in = new FileInputStream(new File(arg));

                if (in == null) {
                    System.err.println("Cannot find " + arg + " on classpath, or in directory " + new File(".").getCanonicalPath());
                    System.err.println("-----------");
                    System.err.println("Please be sure it is located");
                    System.err.println("  - on the classpath and qualified as a classpath location.");
                    System.err.println("  - in the local directory or at a global path in the file system.");
                    continue;
                }

                System.out.println("Initialising properties: " + arg);

                final Configuration configuration = load(in);
                generate(configuration);
            } catch (Exception e) {
                System.err.println("Cannot read " + arg + ". Error : " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        System.out.println("COMPLETE CONVERT");
    }

    private static Configuration load(InputStream in) throws IOException {
        final byte[] buffer = new byte[1000 * 1000];
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int len; (len = in.read(buffer)) >= 0; )
            out.write(buffer, 0, len);

        final String xml = out.toString()
                .replaceAll("<(\\w+:)?configuration\\s+xmlns(:\\w+)?=\"[^\"]*\"[^>]*>", "<$1configuration xmlns$2=\"" + REPO_CONVERTER_XSD + "\">")
                .replace("<configuration>", "<configuration xmlns=\"" + REPO_CONVERTER_XSD + "\">");
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            final JAXBContext ctx = JAXBContext.newInstance(Configuration.class);
            final Unmarshaller unmarshaller = ctx.createUnmarshaller();
            unmarshaller.setSchema(sf.newSchema(Launcher.class.getResource("/xsd/" + CONVERTER_XSD)));
            unmarshaller.setEventHandler(event -> true);
            return (Configuration) unmarshaller.unmarshal(new StringReader(xml));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void generate(Configuration configuration) throws IOException {
        if (!isCorrected(configuration)) {
            System.err.println("Incorrect xml");
            return;
        }

        try {
            setDefault(configuration);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return;
        }

        configuration.getSources().forEach(source -> new Launcher().generate(new File(source.getSourceDir()), "", source));
    }

    public static String preTrim(String str) {
        final Matcher matcher = Pattern.compile("^(\\s+)").matcher(str);
        if (!matcher.find()) return str;
        return str.substring(matcher.end(), str.length());
    }

    public static boolean isContinuedLine(String line) {
        final Matcher matcher = Pattern.compile("(\\\\+)$").matcher(line);
        return matcher.find() && matcher.group().length() % 2 == 1;
    }

    private void generate(File sourceDir, String subDir, Source source) {
        if (!sourceDir.exists())
            sourceDir = new File(sourceDir.getAbsolutePath());

        final File[] childFiles = sourceDir.listFiles();
        if (childFiles == null)
            return;

        final String dir = source.getTargetDir();
        final File targetDir = new File(new File(dir
                + (dir.length() > 0 && dir.length() - 1 != dir.lastIndexOf(File.separatorChar) ? File.separatorChar : "")
                + subDir).getAbsolutePath());

        if (!targetDir.mkdirs() && !targetDir.isDirectory()) {
            System.err.println(targetDir.getAbsolutePath() + " is not directory.");
            return;
        }

        Convertible converter;
        try {
            converter = (Convertible) Class.forName(source.getConverter().trim()).getConstructor(new Class[]{}).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        for (File file : childFiles) {
            if (file.exists() && !source.isOverwrite())
                continue;

            if (source.isToMessageProperties()) {
                converter.decode(file, source.getTargetDir(), source.getTargetEncoding(), source.isDescribeByUnicode());
            } else {
                final String fileName = file.getName();

                final Pattern pattern = Pattern.compile(MESSAGES_FILE_FORMAT);
                final Matcher matcher = pattern.matcher(fileName);
                if (!matcher.find())
                    continue;

                final String languageName = matcher.group(1);

                final String fileSubName = languageName != null && languageName.trim().length() > 0 ? languageName : "default";
                final File targetFile = new File(targetDir, converter.encodingFileName(fileSubName));
                if (targetFile.exists())
                    if (!source.isOverwrite() || targetFile.isDirectory())
                        continue;

                try {
                    final String originCode = new String(Files.readAllBytes(Paths.get(file.toURI())), source.getSourceEncoding());
                    converter.encode(originCode, fileSubName, targetFile, source.getTargetEncoding(), source.isDescribeByUnicode());
                    System.out.println("   converted: " + file.getAbsolutePath() + " -> " + targetFile.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
