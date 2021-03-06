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

import org.json.simple.JSONObject;

import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;

/**
 * @author Benjamin Bouguet
 *
 */
public interface IContextBuilder {

	/**
	 * @param The JSONObject containing the data 
	 * @param metadata The metadata of the report
	 * @param context The context of the report
	 */
	void build(JSONObject json, FieldsMetadata metadata, IContext context);
}
