import org.grails.plugin.hibernate.filter.HibernateFilterDomainConfiguration;

// usiamo questo driver perché quelli più nuovi danno problemi con le full-join (tipo che non funzionano)
class MyDialect extends org.hibernate.dialect.Oracle8iDialect {
	boolean forceLimitUsage() { return true; }
}

dataSource {
	pooled  	= false
	dialect 	= MyDialect //'org.hibernate.dialect.OracleDialect'
	configClass = HibernateFilterDomainConfiguration
}

hibernate {
	cache.use_second_level_cache 	= true
	cache.use_query_cache 			= true
	cache.region.factory_class 		= 'org.hibernate.cache.SingletonEhCacheRegionFactory' // Hibernate 3
//	cache.region.factory_class 		= 'org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory' // Hibernate 4
	singleSession 					= true
	flush.mode 						= 'auto' // oppure 'manual'
	jdbc.use_get_generated_keys 	= true
}

// environment specific settings
environments {
	development {
		dataSource {
			dbCreate = "validate"
//			jndiName = "jdbc/agsde2"		// questo serve per generare i DTO
			jndiName = "java:comp/env/jdbc/agsde2"
		}
		
		dataSource_gdm {
			jndiName = "java:comp/env/jdbc/gdm"
            dialect 	= MyDialect //'org.hibernate.dialect.OracleDialect'
//			jndiName = "jdbc/gdm"
		}

//		dataSource_cf {
//			dialect  = 'org.hibernate.dialect.Oracle8iDialect' //'org.hibernate.dialect.OracleDialect'
//			dbCreate = 'none'
//			jndiName = "java:comp/env/jdbc/cf"
//			persistenceInterceptor = true
//		}
		
		// questo bean viene utilizzato solo dall'integrazione con gs4
//			dataSource_gs4 {
//			jndiName = "java:comp/env/jdbc/gs4"
//		}
	}
	
	test {
		dataSource {
//			pooled = true
//		    url = "jdbc:oracle:thin:@test-consags:1521:aggags"
//		    driverClassName = "oracle.jdbc.driver.OracleDriver"
//		    username = "agsde2"
//		    password = ""
            dialect  = MyDialect //'org.hibernate.dialect.OracleDialect'
			jndiName = "java:comp/env/jdbc/agsde2"
		}

		dataSource_gdm {
			jndiName = "java:comp/env/jdbc/gdm"
            dialect  = MyDialect //'org.hibernate.dialect.OracleDialect'
		}

		dataSource_cf {
			dbCreate = 'none'
			jndiName = "java:comp/env/jdbc/cf"
            dialect  = MyDialect //'org.hibernate.dialect.OracleDialect'
		}
		
		// questo bean viene utilizzato solo dall'integrazione con gs4
//		dataSource_gs4 {
//			jndiName = "java:comp/env/jdbc/gs4"
//		}
	}

	// datasource configurati di default per la produzione
	production {
		dataSource {
			dbCreate = 'validate'
            dialect  = MyDialect //'org.hibernate.dialect.OracleDialect'
			jndiName = "java:comp/env/jdbc/agsde2"
		}
		
		dataSource_gdm {
            dialect  = MyDialect //'org.hibernate.dialect.OracleDialect'
			jndiName = "java:comp/env/jdbc/gdm"
		}
		
		// i datasource che seguono non vengono installati di default.
		// per attivarli in produzione dai clienti che li richiedono, bisogna fare:
		// 1) modificare il file conf/Catalina/localhost/Atti.xml aggiungendo l'opportuna configurazione jndi
		// 2) modificare il file confapps/Atti/DataSource.groovy specificando i datasource
		
		// questo bean viene utilizzato solo dall'integrazione con la contabilità finanziaria tramite plugin
//		dataSource_cf {
//			dbCreate = 'none'
//			jndiName = "java:comp/env/jdbc/cf"
//		}
		
		// questo bean viene utilizzato solo dall'integrazione con gs4
//		dataSource_gs4 {
//			jndiName = "java:comp/env/jdbc/gs4"
//		}
	}
}

