package com.oyameen.SpringBootBatchFileUploadBasics.config;

import com.oyameen.SpringBootBatchFileUploadBasics.model.Employee;
import com.oyameen.SpringBootBatchFileUploadBasics.repository.EmployeeRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
//@EnableBatchProcessing //commented to allow to create batches table
public class BatchConfig {

    private static final int chunkSize = 10;
    private static final int concurrencyLimit = 10;
    @Autowired
    private EmployeeRepository employeeRepository;

    @Bean
    @StepScope
    public FlatFileItemReader<Employee> flatFileItemReader(@Value("#{jobParameters[filePath]}") String filePath) {
        FlatFileItemReader<Employee> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setName("EmployeeItemReader");
        flatFileItemReader.setResource(new FileSystemResource(filePath));
        flatFileItemReader.setLinesToSkip(1);
        flatFileItemReader.setLineMapper(employeeItemReaderLineMapper());
        return flatFileItemReader;
    }

    @Bean
    public EmployeeItemProcessor employeeItemProcessor() {
        return new EmployeeItemProcessor();
    }

    @Bean
    public RepositoryItemWriter<Employee> repositoryItemWriter() {
        RepositoryItemWriter<Employee> repositoryItemWriter = new RepositoryItemWriter<>();
        repositoryItemWriter.setRepository(employeeRepository);
        repositoryItemWriter.setMethodName("save");
        return repositoryItemWriter;
    }

    @Bean
    public Step step(FlatFileItemReader<Employee> employeeItemReader, JobRepository jobRepository,
                     PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("importEmployees-step", jobRepository)
                .<Employee, Employee>chunk(chunkSize, platformTransactionManager)
                .reader(employeeItemReader)
                .processor(employeeItemProcessor())
                .writer(repositoryItemWriter())
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor simpleAsyncTaskExecutor = new SimpleAsyncTaskExecutor();
        simpleAsyncTaskExecutor.setConcurrencyLimit(concurrencyLimit);
        return simpleAsyncTaskExecutor;
    }

    @Bean
    public Job job(FlatFileItemReader<Employee> employeeItemReader, JobRepository jobRepository,
                   PlatformTransactionManager platformTransactionManager) {
        return new JobBuilder("importEmployees-job", jobRepository)
                .flow(step(employeeItemReader, jobRepository, platformTransactionManager))
                .end()
                .build();
    }

    private LineMapper<Employee> employeeItemReaderLineMapper() {

        DefaultLineMapper<Employee> employeeItemReaderLineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer employeeLineTokenizer = new DelimitedLineTokenizer();
        employeeLineTokenizer.setDelimiter(",");
        employeeLineTokenizer.setNames("id", "firstName", "lastName", "country", "phoneNo", "salary", "age", "jobTitle");
        employeeLineTokenizer.setStrict(false);

        BeanWrapperFieldSetMapper<Employee> employeeBeanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
        employeeBeanWrapperFieldSetMapper.setTargetType(Employee.class);

        employeeItemReaderLineMapper.setLineTokenizer(employeeLineTokenizer);
        employeeItemReaderLineMapper.setFieldSetMapper(employeeBeanWrapperFieldSetMapper);
        return employeeItemReaderLineMapper;

    }

}
