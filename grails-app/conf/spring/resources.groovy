import org.springframework.boot.jdbc.health.DataSourceHealthIndicator

// Extra Spring beans, written in Groovy's "Spring Bean DSL".
// Spring Boot equivalent: a @Configuration class full of @Bean methods.
//
// Each line is   beanName(BeanClass, constructorArg1, constructorArg2, ...)
// and ref('otherBean') injects another bean by its name, like a @Bean method parameter would.
beans = {
    // Adds a "db" check to /actuator/health: it runs a validation query against the database.
    // Spring Boot normally adds this by itself, but its auto-configuration only does so when a
    // DataSource bean ALREADY exists when auto-configuration runs. Grails (GORM) registers its
    // "dataSource" bean later than that, so Boot skips it and we register it ourselves.
    // The health component is named after the bean minus "HealthIndicator", so this becomes "db".
    dbHealthIndicator(DataSourceHealthIndicator, ref('dataSource'))
}
