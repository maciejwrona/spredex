package com.maciej.spredex.Sheet;

import java.util.Collection;
import java.util.List;

import com.maciej.spredex.CellCoordinates;
import com.maciej.spredex.CellLoc;

public interface DependencyGraph {
	public void updateRequired(CellLoc by, Collection<CellCoordinates> required);
	public List<CellCoordinates> required(CellLoc by);
	public List<CellCoordinates> dependent(CellLoc on);
	public List<CellLoc> getUpdateOrder(CellLoc start);
}
