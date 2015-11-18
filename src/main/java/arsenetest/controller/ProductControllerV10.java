package arsenetest.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

/**
 * Controller providing CRUD and schema modification operations for Product entities
 * 
 * @author akazimirski
 *
 */
@Controller
@RequestMapping("/api/v10/products")
public class ProductControllerV10 extends CRUDController {
	
	private static final String SCHEMA_PARAMETER_NAME = "schema";
	final Map<String, String> restByDatastoreMapping = new HashMap<>();
	{
		restByDatastoreMapping.put("name", "name");
		restByDatastoreMapping.put("group", "group");
	}
	
	@Override
	protected String getEntityName() {
		return "Product";
	}
	
	@Override
	protected Map<String, String> getRestByDatastoreMapping() {
		return restByDatastoreMapping;
	}
	
	@RequestMapping(value = "/{id}/schema", method = RequestMethod.GET)
	public @ResponseBody String getEntitySchema(@PathVariable String id, ModelMap model) {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Entity ent = readEntity(id, ds);
		return (String)ent.getProperty(SCHEMA_PARAMETER_NAME);
	}

	@RequestMapping(value = "/{id}/schema", method = RequestMethod.PUT, consumes = {"application/json"})
	public @ResponseBody ResponseEntity<String> saveEntitySchema(@PathVariable String id, @RequestBody String schema, ModelMap model) {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Entity ent = readEntity(id, ds);
		ent.setProperty(SCHEMA_PARAMETER_NAME, schema);
		ds.put(ent);
		return new ResponseEntity<>(HttpStatus.OK);
	}
}