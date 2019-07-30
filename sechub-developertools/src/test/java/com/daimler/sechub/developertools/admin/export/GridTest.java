package com.daimler.sechub.developertools.admin.export;

import static org.junit.Assert.*;

import org.junit.Test;

public class GridTest {

	@Test
	public void a_b_c() {
		/* prepare + execute */
		Grid grid = new Grid(Row.builder().add("a").add("b").add("c").build());
		/* test */
		assertEquals("\"a\",\"b\",\"c\"", grid.toCSVString());
	}

	@Test
	public void a_b_c__1_2_3() {
		/* prepare + execute */
		Grid grid = new Grid(Row.builder().add("a").add("b").add("c").build());
		grid.add(Row.builder().add(1).add(2).add(3).build());
		/* test */
		assertEquals("\"a\",\"b\",\"c\"\n1,2,3", grid.toCSVString());
	}


	@Test
	public void a_b_c__1_2_3__x_y_z() {
		/* prepare + execute */
		Grid grid = new Grid(Row.builder().add("a").add("b").add("c").build());
		grid.add(Row.builder().add(1).add(2).add(3).build());
		grid.add(Row.builder().add("x").add("y").add("z").build());
		/* test */
		assertEquals("\"a\",\"b\",\"c\"\n1,2,3\n\"x\",\"y\",\"z\"", grid.toCSVString());
	}

}
