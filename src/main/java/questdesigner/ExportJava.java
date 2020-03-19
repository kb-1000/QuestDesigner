package questdesigner;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import javafx.beans.property.SimpleListProperty;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class ExportJava {
    private ExportJava() {
        throw new UnsupportedOperationException("no instances for you!");
    }

    public static void write(Path path, TextItem textItem, String packageName, String className) throws IOException {
        if (textItem.getResponse() != null && textItem.getResponse().length() > 0) {
            throw new IllegalArgumentException("Root item may not have user response");
        }
        JavaFile.builder(packageName,
                TypeSpec.classBuilder(className)
                        .addModifiers(Modifier.PUBLIC)
                        .superclass(ClassName.get("com.parzivail.pswm.quest", "Quest"))
                        .addMethod(MethodSpec.constructorBuilder()
                                .addModifiers(Modifier.PUBLIC)
                                .addCode(convertToCodeBlock(textItem, "tree"))
                                .build())
                        .build())
                .indent("    ")
                .build()
                .writeTo(path, StandardCharsets.UTF_8);
    }

    private static ClassName DialogTree = ClassName.get("com.parzivail.pswm.quest", "DialogTree");

    private static CodeBlock convertToCodeBlock(TextItem textItem, String base) {
        final CodeBlock.Builder codeBlock = CodeBlock.builder()
                .add(base + " = new $T();\n", DialogTree);
        final SimpleListProperty<TextItem> subItems = textItem.subItemsProperty();
        final int subItemCount = subItems.size();
        if (subItemCount > 3) {
            throw new IllegalArgumentException("PSWM DialogTree only supports up to 3 user reponses per item");
        }
        final String text = textItem.getText();
        if (text != null && text.length() > 0) {
            codeBlock.add(base + ".npcHeader = $S;\n", text);
        }
        for (int i = 0; i < subItemCount; i++) {
            final TextItem subItem = subItems.get(i);
            final String response = subItem.getResponse();
            if (response != null && response.length() > 0) {
                codeBlock.add(base + ".response" + (i + 1) + " = $S;\n", response);
            }
            codeBlock.add(convertToCodeBlock(subItem, base + ".response" + (i + 1) + "DT"));
        }
        return codeBlock.build();
    }
}
