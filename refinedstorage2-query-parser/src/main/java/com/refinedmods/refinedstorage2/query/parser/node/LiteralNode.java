package com.refinedmods.refinedstorage2.query.parser.node;

import com.refinedmods.refinedstorage2.query.lexer.Token;
import com.refinedmods.refinedstorage2.query.lexer.TokenRange;

public record LiteralNode(Token token) implements Node {
    public Token getToken() {
        return token;
    }

    @Override
    public String toString() {
        return token.getContent();
    }

    @Override
    public TokenRange getRange() {
        return token.getPosition().getRange();
    }
}
