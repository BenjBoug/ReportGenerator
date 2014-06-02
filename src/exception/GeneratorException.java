/**
 * Copyright (C) 2013 Benjamin Bouguet, Paul Chaignon
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
package exception;

import generator.GeneratorError;

public class GeneratorException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private GeneratorError error;
	
	public GeneratorException(GeneratorError error, String msg) {
		super(msg);
		this.error=error;
	}

	public GeneratorError getError() {
		return error;
	}
}
