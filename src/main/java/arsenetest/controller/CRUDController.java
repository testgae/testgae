package arsenetest.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import arsenetest.controller.dto.DTO;
import arsenetest.controller.exception.NotFoundException;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

/**
 * Base implementation of a controller providing CRUD operations for any entity
 * 
 * @author akazimirski
 *
 */
public abstract class CRUDController {

	private static final String KEY_PARAMETER_NAME = "$ID";

	protected final Logger logger = Logger.getLogger(this.getClass().getName());
	
	// the idea is to have entity name and mappings in a single place
	protected abstract String getEntityName();

	// the idea is to have entity name and mappings in a single place
	protected abstract Map<String, String> getRestByDatastoreMapping();

	protected DTO mapDatasourceEntity(Entity entity, Map<String, String> mapping) {
		DTO result = new DTO();
		result.put(KEY_PARAMETER_NAME, "" + entity.getKey().getId());
		for (Entry<String, String> entry: mapping.entrySet()) {
			result.put(entry.getKey(), entity.getProperty(entry.getValue()));
		}
		return result;
	}

	protected Entity mapRestEntity(DTO obj, Map<String, String> mapping) {
		Entity result = new Entity(getEntityName());
		for (Entry<String, String> entry: mapping.entrySet()) {
			result.setProperty(entry.getValue(), obj.get(entry.getKey()));
		}
		return result;
	}
	
	Entity readEntity(String id, DatastoreService ds) {
		try {
			return ds.get(KeyFactory.createKey(getEntityName(), Long.parseLong(id)));
		} catch(Exception ex) {
			logger.warning("Cannot found entity " + id);
			throw new NotFoundException();
		}
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody ResponseEntity<String> putEntity(@PathVariable String id, @RequestBody DTO obj, ModelMap model) {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Entity ent = readEntity(id, ds);
		Entity newEntity = mapRestEntity(obj, getRestByDatastoreMapping());
		ent.setPropertiesFrom(newEntity);
		ds.put(ent);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public @ResponseBody ResponseEntity<String> deleteEntity(@PathVariable Long id, ModelMap model) {
		Key key = KeyFactory.createKey(getEntityName(), id);
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		ds.delete(key);
		return new ResponseEntity<>(HttpStatus.OK);
	}
	
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody ResponseEntity<DTO> getEntity(@PathVariable String id, ModelMap model) {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Entity ent = readEntity(id, ds);
		return new ResponseEntity<DTO>(mapDatasourceEntity(ent, getRestByDatastoreMapping()), HttpStatus.OK); 
	}

	@RequestMapping(value = "", method = RequestMethod.POST, consumes = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody ResponseEntity<String> addEntity(@RequestBody DTO obj) {
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Entity entity = mapRestEntity(obj, getRestByDatastoreMapping());
		ds.put(entity);
		return new ResponseEntity<String>(HttpStatus.CREATED);
	}

	@RequestMapping(value = "", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	public @ResponseBody List<DTO> getProducts(ModelMap model) {
		//
		List<DTO> result = new ArrayList<DTO>();
		DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
		Query q = new Query(getEntityName());
		PreparedQuery pq = ds.prepare(q);
		for (Entity entity: pq.asIterable()) {
			entity.getKey().getId();
			result.add(mapDatasourceEntity(entity, getRestByDatastoreMapping()));
		}
		return result;
	}
}