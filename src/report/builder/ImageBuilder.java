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

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONObject;

import fr.opensagres.xdocreport.document.images.FileImageProvider;
import fr.opensagres.xdocreport.document.images.IImageProvider;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;

/**
 * @author Benjamin Bouguet
 *
 */
public class ImageBuilder implements IContextBuilder {

	/**
	 * Initializes the FieldsMetaData for all images,
	 * creates them with the paths from the JSON and adds it to the report's context.
	 */
	@Override
	public void build(JSONObject json, FieldsMetadata metadata, IContext context) {
		// iterate on all images
		Iterator<?> iter = json.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<?,?> entry = (Map.Entry<?,?>)iter.next();
			// set the FieldsMetaData
			metadata.addFieldAsImage(entry.getKey().toString());
			// create the image
			IImageProvider img = new FileImageProvider(new File(entry.getValue().toString()));
			img.setUseImageSize(true);
			// add it to the report's context
			context.put(entry.getKey().toString(), img);
		}
	}

}
