/*
 * Copyright 2013-2022 Owen Rubel
 * API Chaining(R) 2022 Owen Rubel
 *
 * Licensed under the AGPL v2 License;
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Owen Rubel (orubel@gmail.com)
 *
 */
package demo.application.service;

import org.springframework.beans.factory.annotation.Autowired;
import demo.application.repositories.DeptRepository;
import demo.application.repositories.HookRepository;
import demo.application.domain.Hook;
import demo.application.domain.User;
import demo.application.service.IDept;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static groovyx.gpars.GParsPool.withPool;
import javax.servlet.http.HttpServletRequest;


@Service
public class HookService implements IHook {


	HookRepository hookrepo;

	//ApplicationContext ctx;

	@Autowired
	public HookService(HookRepository hookrepo) {
		this.hookrepo = hookrepo;
		//	this.ctx = applicationContext
	}


	//public HookService(ApplicationContext applicationContext) {
	//	this.ctx = applicationContext
	//}

	//@Override
	public Hook save(Hook hook){
		// TODO Auto-generated method stub
		hookrepo.save(hook);
		hookrepo.flush();
		return hook;
	}

	@Override
	public void deleteById(Long id){
		// TODO Auto-generated method stub
		hookrepo.deleteById(id);
		hookrepo.flush();
	}

	//@Override
	//public Company findById(int id) {
	//    return comprepo.findById(Long.valueOf(id));
	//}


	//@Override
	public Hook findById(Long id){
		// TODO Auto-generated method stub
		return hookrepo.findById(id).get();
	}

	//@Override
	public List<Hook> findByUser(User user){
		// TODO Auto-generated method stub
		return hookrepo.findByUser(user);
	}

	/*
	* NOTE : we use 'service' rather than endpoint to declare a 'controller'.
	* This enables us to use PUT/POST/DELETE for controller to report
	* all changes.
	 */
	public Hook findByServiceAndUser(String service, User user){
		return hookrepo.findByServiceandUser(service,user).get();
	}

	/*
	* NOTE : we use 'service' rather than endpoint to declare a 'controller'.
	* This enables us to use PUT/POST/DELETE for controller to report
	* all changes.
	 */
	public List<Hook> findByEnabledTrueAndService(String service){
		return hookrepo.findByEnabledTrueAndService(service).get();
	}

	/**
	 * Given the data to be sent and 'service' for which hook is defined,
	 * will send data to all 'subscribers'
	 * @param String URI of local endpoint being hooked into
	 * @param String data to be sent to all subscribers
	 * @return
	 */
	private boolean send(String data, String service) {
		def hooks = findByEnabledTrueAndService(service);
		//def hooks = Class.forName('net.nosegrind.apiframework.Hook').findAll("from Hook where is_enabled=true and service=?",[service])

		GParsPool.withPool(this.cores){ pool ->

			HttpURLConnection myConn = null;
			DataOutputStream os = null;
			BufferedReader stdInput = null;

			hooks.eachParallel(){ Hook hook ->
				String format = hook.format.toLowerCase();
				if (hook.attempts >= grailsApplication.config.apitoolkit.attempts) {
					data = [message: 'Number of attempts exceeded. Please reset hook via web interface'];
				}


				try {
					URL hostURL = new URL(hook.callback.toString());
					myConn = (HttpURLConnection) hostURL.openConnection();
					myConn.setRequestMethod("POST");
					myConn.setRequestProperty("Content-Type", "application/json");
					if (hook?.authorization) {
						myConn.setRequestProperty("Authorization", "${hook.authorization}");
					}
					myConn.setUseCaches(false);
					myConn.setDoInput(true);
					myConn.setDoOutput(true);
					myConn.setReadTimeout(15 * 1000);

					myConn.connect();

					OutputStreamWriter out = new OutputStreamWriter(myConn.getOutputStream());
					out.write(data);
					out.close();

					int code = myConn.getResponseCode();
					myConn.diconnect();

					return code;
				} catch (Exception e) {
					try {
						Thread.sleep(15000);
					} catch (InterruptedException ie) {
						println(e);
					}
				} finally {
					if (myConn != null) {
						myConn.disconnect();
					}
				}
				return 400;
			}
		}
	}

	/**
	 * Given the data to be sent and 'service' for which hook is defined,
	 * will send error message to all 'subscribers'
	 * @param String URI of local endpoint being hooked into
	 * @param String data to be sent to all subscribers
	 * @return
	 */
	private boolean sendErr(String data, String service) {
		def hooks = findByEnabledTrueAndService(service);
		//def hooks = grailsApplication.getClassForName('net.nosegrind.apiframework.Hook').findAll("from Hook where is_enabled=true and service=?",[service])

		hooks.each { hook ->
			String format = hook.format.toLowerCase();

			String message = 	[message:data];


			HttpURLConnection myConn= null;
			DataOutputStream os = null;
			BufferedReader stdInput = null;
			try{
				URL hostURL = new URL(hook.callback.toString());
				myConn= (HttpURLConnection)hostURL.openConnection();
				myConn.setRequestMethod("POST");
				myConn.setRequestProperty("Content-Type", "application/json");
				if(hook?.authorization) {
					myConn.setRequestProperty("Authorization", "${hook.authorization}");
				}
				myConn.setUseCaches(false);
				myConn.setDoInput(true);
				myConn.setDoOutput(true);
				myConn.setReadTimeout(15*1000);

				myConn.connect();

				OutputStreamWriter out = new OutputStreamWriter(myConn.getOutputStream());
				out.write(message);
				out.close();

				int code =  myConn.getResponseCode();
				myConn.disconnect();

				return code;
			}catch (Exception e){
				try{
					Thread.sleep(15000);
				}catch (InterruptedException ie){
					println(e);
				}
			} finally{
				if (myConn!= null){
					myConn.disconnect();
				}
			}
			return 400;
		}
	}

	/**
	 * Determines if object is valid domain class and return object as a Map
	 * @param Object an object assumed to be a domain object
	 * @return Data as a Map
	 */
	Map formatDomainObject(Object data){
		def nonPersistent = ["log", "class", "constraints", "properties", "errors", "mapping", "metaClass","maps"];
		def newMap = [:];
		data.getProperties().each { key, val ->
			if (!nonPersistent.contains(key)) {
				if(grailsApplication.isDomainClass(val.getClass())){
					newMap.put key, val.id;
				}else{
					newMap.put key, val;
				}
			}
		}
		return newMap;
	}

	/*
	Map processMap(Map data,Map processor){
		processor.each() { key, val ->
			if(!val?.trim()){
				data.remove(key)
			}else{
				def matcher = "${data[key]}" =~ "${data[key]}"
				data[key] = matcher.replaceAll(val)
			}
		}
		return data
	}

	boolean validateUrl(String url){
		try {
			String[] schemes = ["http", "https"]
			UrlValidator urlValidator = new UrlValidator(schemes)
			if (urlValidator.isValid(url)) {
				return true
			} else {
				return false
			}
		}catch(Exception e){
			println(e)
		}
		return false
	}
	*/
}
