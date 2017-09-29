package com.vaadin.sass.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import io.bit3.jsass.CompilationException;
import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;
import io.bit3.jsass.Output;
import io.bit3.jsass.OutputStyle;
import io.bit3.jsass.importer.Import;

/**
 * @author Andreas Asplund <a href="mailto:andreas@asplund.biz">andreas@asplund.biz</a>
 */
public class JSassCompiler {
    private final SassConfiguration sassConfiguration;

    public JSassCompiler(SassConfiguration configuration) {
        this.sassConfiguration = configuration;
    }

    public String compile(String sassFile) {
        Compiler compiler = new Compiler();
        Options options = new Options();
        File outputMapFile = new File(sassFile.replaceFirst("scss$", "css.map"));
        options.setSourceMapFile(outputMapFile.toURI());
        options.getImporters().add(this::getImports);
        options.setOutputStyle(OutputStyle.COMPRESSED);

        try {
            File outputFile = new File(sassFile.replaceFirst("scss$", "css"));
            String scss = read(getInputStream(getFileName("", sassFile)));
            if (scss == null) {
                try {
                    scss = read(new FileInputStream(sassFile));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            if (sassFile.endsWith("styles.scss")) {
                scss = "$v-relative-paths: false;\n" + scss;
            }
            Output output = compiler.compileString(scss, new File(sassFile).toURI(), outputFile.toURI(), options);
            try (PrintWriter pw = new PrintWriter(outputMapFile)) {
                pw.append(output.getSourceMap());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            return output.getCss();
        } catch (CompilationException e) {
            throw new RuntimeException(e);
        }
    }

    private Collection<Import> getImports(String theImport, Import parent) {
        String path = parent.getAbsoluteUri().getPath().replace('\\', '/').substring(0, parent.getAbsoluteUri().getPath().replace('\\', '/').lastIndexOf('/') + 1);
        String fileName = getFileName(path, underscore(theImport));
        InputStream is = getInputStream(fileName);

        if (is == null) {
            fileName = getFileName(path, theImport);
            is = getInputStream(fileName);
        }

        if (is != null) {
            try {
                return Collections.singleton(new Import(theImport, fileName, read(is)));
            } catch (URISyntaxException ignore) {
            }
        }
        return null;
    }

    private static InputStream getInputStream(String filename) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
    }

    private String read(InputStream is) {
        if (is != null) {
            try (BufferedReader buffer = new BufferedReader(new InputStreamReader(is))) {
                return buffer.lines().collect(Collectors.joining("\n"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    private String underscore(String s) {
        int idx = s.lastIndexOf('/');
        if (idx > -1) {
            return s.substring(0, idx + 1) + "_" + s.substring(idx + 1);
        }

        return "_" + s;
    }

    private String getFileName(String path, String identifier) {
        String fileName = path + identifier;
        fileName = toAbsolutePath(fileName);
        if (!fileName.endsWith(".css") && !fileName.endsWith(".scss")) {
            fileName += ".scss";
        }

        // Filename should be a relative path starting with VAADIN/...
        int vaadinIdx = fileName.lastIndexOf("VAADIN/");
        if (vaadinIdx > -1) {
            fileName = fileName.substring(vaadinIdx);
        }
        return fileName;
    }

    public static String toAbsolutePath(String maybeRelative) {
        Path path = Paths.get(maybeRelative);
        Path effectivePath = path;
        if (!path.isAbsolute()) {
            Path base = Paths.get("");
            effectivePath = base.resolve(path).toAbsolutePath();
        }
        return effectivePath.normalize().toString().replace('\\', '/');
    }

    public SassConfiguration getSassConfiguration() {
        return sassConfiguration;
    }

    public List<String> getSources() {
        List<Path> mapFiles = listCssMapFiles(sassConfiguration);
        Gson gson = new Gson();
        CssMap cssMap = gson.fromJson(readTextFile(mapFiles.get(0)), CssMap.class);
        return cssMap.getSources();
    }

    private static List<Path> listCssFiles(SassConfiguration sassConfiguration) {
        return listFiles(sassConfiguration.projectPath + "/" + sassConfiguration.cssDirectory, "*.css");
    }

    private static List<Path> listCssMapFiles(SassConfiguration sassConfiguration) {
        return listFiles(sassConfiguration.projectPath + "/" + sassConfiguration.cssDirectory, "*.css.map");
    }

    private static List<Path> listFiles(String directory, String fileEnding) {
        List<Path> result = new ArrayList<>();
        Path dir = Paths.get(directory);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, fileEnding)) {
            for (Path entry : stream) {
                result.add(entry);
            }
        } catch (DirectoryIteratorException | IOException ex) {
            throw new RuntimeException(ex);
        }
        return result;
    }

    public static String readTextFile(Path file) {
        try {
            return new String(Files.readAllBytes(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static class SassConfiguration {
        public String projectPath = null;
        public String cssDirectory = ".";
    }

    private static class CssMap {
        private Integer version;
        private String mappings;
        private List<String> sources;
        private List<String> names;
        private String file;

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getMappings() {
            return mappings;
        }

        public void setMappings(String mappings) {
            this.mappings = mappings;
        }

        public List<String> getNames() {
            return names;
        }

        public void setNames(List<String> names) {
            this.names = names;
        }

        public List<String> getSources() {
            return sources;
        }

        public void setSources(List<String> sources) {
            this.sources = sources;
        }

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }
    }
}
