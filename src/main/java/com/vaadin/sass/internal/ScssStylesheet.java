/*
 * Copyright 2000-2014 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.sass.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.List;
import java.util.stream.Collectors;

import com.yahoo.platform.yui.compressor.CssCompressor;

public class ScssStylesheet {
    private JSassCompiler jSassCompiler;
    private String css;
    private String identifier;

    public static ScssStylesheet get(String identifier) throws IOException {
        ScssStylesheet scss = new ScssStylesheet();
        scss.identifier = identifier;
        JSassCompiler.SassConfiguration configuration = new JSassCompiler.SassConfiguration();
        configuration.projectPath = identifier.substring(0, identifier.lastIndexOf(System.getProperty("file.separator")) + 1);
        scss.jSassCompiler = new JSassCompiler(configuration);
        return scss;
    }

    public List<String> getSourceUris() {
        return jSassCompiler.getSources().stream().map(s -> jSassCompiler.getSassConfiguration().projectPath + s).collect(Collectors.toList());
    }

    public void compile() throws Exception {
        this.css = jSassCompiler.compile(identifier);
    }

    public String printState() {
        if (this.css == null) {
            try {
                compile();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        String css = this.css;
        this.css = null;
        return css;
    }

    public void write(Writer writer) throws IOException {
        write(writer, false);
    }

    public void write(Writer writer, boolean minify) throws IOException {
        if (minify) {
            InputStreamReader reader = new InputStreamReader(
                    new ByteArrayInputStream(printState().getBytes("UTF-8")));
            CssCompressor compressor = new CssCompressor(reader);
            compressor.compress(writer, -1);
        } else {
            writer.write(printState());
        }
    }
}
