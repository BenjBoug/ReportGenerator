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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;

/**
 * @author Benjamin Bouguet
 *
 */
public class ListBuilder implements IContextBuilder {

	/**
	 * Initializes the FieldsMetaData with the key of the JSON data
	 * and adds the list to the report's context.
	 * @param listObject The JSON list object.
	 */
	@Override
	public void build(JSONObject json, FieldsMetadata metadata, IContext context) {
		// Iterates on all the list:
				Iterator<?> iter = json.entrySet().iterator();
				while (iter.hasNext()) {
					// Gets the key and the array:
					Map.Entry<?,?> entry = (Map.Entry<?,?>)iter.next();
					String key = entry.getKey().toString();
					JSONArray array = (JSONArray) entry.getValue();
			
					// Gets the FieldsMetaData, searching for all different keys on the Map:
					HashSet<String> metaData = new HashSet<String>();
					Iterator<?> iterArray = array.iterator();
					while (iterArray.hasNext()) {
						JSONObject map = (JSONObject) iterArray.next();
						Iterator<?> iterMap = map.entrySet().iterator();
						while (iterMap.hasNext()) {
							Map.Entry<?,?> entryMap = (Map.Entry<?,?>)iterMap.next();	
							metaData.add(entryMap.getKey().toString());
						}
					}
					
					// Sets the FieldsMetaData:
					Iterator<?> iterMetaData = metaData.iterator();
					while (iterMetaData.hasNext()) {
						metadata.addFieldAsList(key+"."+iterMetaData.next().toString());	
					}
			
					// Adds the array to the report's context.
					// array can be directly passed as a value for contextMap because it inherits from ArrayList.
					context.put(key, array);
				} 
	}

}
