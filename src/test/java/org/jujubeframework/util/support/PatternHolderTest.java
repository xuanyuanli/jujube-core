package org.jujubeframework.util.support;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class PatternHolderTest {

    @Test
    public void escapeExprSpecialWord() {
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("$")).isEqualTo("\\$");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("?")).isEqualTo("\\?");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("(")).isEqualTo("\\(");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord(")")).isEqualTo("\\)");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("*")).isEqualTo("\\*");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("+")).isEqualTo("\\+");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord(".")).isEqualTo("\\.");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("[")).isEqualTo("\\[");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("]")).isEqualTo("\\]");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("^")).isEqualTo("\\^");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("{")).isEqualTo("\\{");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("}")).isEqualTo("\\}");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("|")).isEqualTo("\\|");
        Assertions.assertThat(PatternHolder.escapeExprSpecialWord("\\")).isEqualTo("\\\\");
    }
}