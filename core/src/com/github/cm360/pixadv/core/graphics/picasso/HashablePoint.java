package com.github.cm360.pixadv.core.graphics.picasso;

import com.badlogic.gdx.math.GridPoint2;

public class HashablePoint extends GridPoint2 {

	private static final long serialVersionUID = -1761668980804904928L;

	public HashablePoint(GridPoint2 p) {
		super(p);
	}
	
	public HashablePoint(int x, int y) {
		super(x, y);
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

}
