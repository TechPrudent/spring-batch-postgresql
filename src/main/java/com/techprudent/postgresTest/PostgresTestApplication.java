package com.techprudent.postgresTest;

import java.util.List;

import org.json.JSONObject;
import org.postgresql.util.PGobject;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootApplication
public class PostgresTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(PostgresTestApplication.class, args);
	}

}

class PeopleItemProcessor implements ItemProcessor<People, People> {

	@Autowired
	private JdbcTemplate jt;

	private static long count = 1;

	@Override
	public People process(final People people) throws Exception {

		PGobject jsonObject = new PGobject();
		jsonObject.setType("json");
		jsonObject.setValue(new JSONObject(people).toString());

		jt.update("INSERT INTO postgres.data (data) VALUES (?)", jsonObject);
		count++;
		
//		if(count > 10000 && count % 10000 == 0) {
//			Thread.sleep(20 * 1000);
//			System.out.println("Sleeping for the count: " + count);
//		}
		 
		return people;
	}

}

class NoOpItemWriter implements ItemWriter<People> {
	@Override
	public void write(List<? extends People> items) throws Exception {
	}
}

@Configuration
@EnableBatchProcessing
class BatchConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Bean
	public FlatFileItemReader<People> reader() {
		return new FlatFileItemReaderBuilder<People>().name("PeopleItemReader")
				.resource(new ClassPathResource("Sample - Superstore.csv")).delimited()
				.names(new String[] { "rowId", "orderId", "orderDate", "shipDate", "customerId", "customerName",
						"segment", "city", "state", "sales", "quantity", "discount", "profit" })
				.fieldSetMapper(new BeanWrapperFieldSetMapper<People>() {
					{
						setTargetType(People.class);
					}
				}).build();
	}

	@Bean
	public PeopleItemProcessor processor() {
		return new PeopleItemProcessor();
	}

	@Bean
	public ItemWriter<People> writer() {
		return new NoOpItemWriter();
	}

	@Bean
	public Job importUserJob() {
		return jobBuilderFactory.get("importUserJob").incrementer(new RunIdIncrementer()).flow(step1()).end().build();
	}

	@Bean
	public TaskExecutor taskExecutor() {
		SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
		taskExecutor.setConcurrencyLimit(5);
		return taskExecutor;
	}

	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1").<People, People>chunk(100).reader(reader()).processor(processor())
				.writer(writer()).taskExecutor(taskExecutor()).throttleLimit(10).build();
	}

}
