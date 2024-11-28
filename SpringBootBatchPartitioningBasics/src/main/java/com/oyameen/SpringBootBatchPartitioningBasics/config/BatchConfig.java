package com.oyameen.SpringBootBatchPartitioningBasics.config;

import com.oyameen.SpringBootBatchPartitioningBasics.model.Employee;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
//@EnableBatchProcessing //commented to allow to create batches table
public class BatchConfig {

    private static final int chunkSize = 125;
    private static final int gridSize = 8;
    private static final int threadPoolSize = 16;
    @Autowired
    private EmployeeItemWriter employeeWriter;

    @Bean
    public FlatFileItemReader<Employee> flatFileItemReader() {
        FlatFileItemReader<Employee> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setName("EmployeeItemReader");
        flatFileItemReader.setResource(new FileSystemResource("src/main/resources/Employees.csv"));
        flatFileItemReader.setLinesToSkip(1);
        flatFileItemReader.setLineMapper(employeeItemReaderLineMapper());
        return flatFileItemReader;
    }

    @Bean
    public EmployeeItemProcessor employeeItemProcessor() {
        return new EmployeeItemProcessor();
    }


    @Bean
    public Step secondaryStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("importEmployees-secondaryStep", jobRepository)
                .<Employee, Employee>chunk(chunkSize, platformTransactionManager)
                .reader(flatFileItemReader())
                .processor(employeeItemProcessor())
                .writer(employeeWriter)
                .build();
    }

    @Bean
    public Step mainStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new StepBuilder("importEmployees-mainStep", jobRepository)
                .partitioner(secondaryStep(jobRepository, platformTransactionManager).getName(), importEmployeesPartitioner())
                .partitionHandler(partitionHandler(jobRepository, platformTransactionManager))
                .build();
    }

    @Bean
    public ImportEmployeesPartitioner importEmployeesPartitioner() {
        return new ImportEmployeesPartitioner();
    }

    @Bean
    public PartitionHandler partitionHandler(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        TaskExecutorPartitionHandler taskExecutorPartitionHandler = new TaskExecutorPartitionHandler();
        taskExecutorPartitionHandler.setGridSize(gridSize);
        taskExecutorPartitionHandler.setTaskExecutor(taskExecutor());
        taskExecutorPartitionHandler.setStep(secondaryStep(jobRepository, platformTransactionManager));
        return taskExecutorPartitionHandler;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(threadPoolSize);
        threadPoolTaskExecutor.setMaxPoolSize(threadPoolSize);
        threadPoolTaskExecutor.setQueueCapacity(threadPoolSize);
        return threadPoolTaskExecutor;
    }

    @Bean
    public Job job(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager) {
        return new JobBuilder("importEmployees-job", jobRepository)
                .flow(mainStep(jobRepository, platformTransactionManager))
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
