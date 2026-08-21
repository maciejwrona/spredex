package com.maciej.spredex.Parser;

import com.maciej.spredex.Parser.Expressions.Expression;
import com.maciej.spredex.Cell.CellRef;

import java.util.List;

public record ParseResult(Expression ast, List<CellRef> requires) {}
