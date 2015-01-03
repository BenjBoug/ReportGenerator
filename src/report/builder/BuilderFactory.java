/**
 * Copyright (C) 2013 Benjamin Bouguet
 *
 * ReportGenerator is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * ReportGenerator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package report.builder;

/**
 * @author Benjamin Bouguet
 *
 */
public class BuilderFactory {

	/**
	 * Create the builder for the given string (type of data)
	 * @param str the type of data
	 * @return the builder associate to the type of data
	 */
	public static IContextBuilder getBuilder(String str)
	{
		if (str.equals("text"))
			return new TextBuilder();
		else if (str.equals("image"))
			return new ImageBuilder();
		else if (str.equals("list"))
			return new ListBuilder();
		else if (str.equals("barcode"))
			return new BarCodeBuilder();
		else 
			return new TextBuilder();
	}
}
