package com.project.pandemic;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.project.pandemic.domain.*;

@SpringBootApplication
public class PandemicApplication {
	
	private static final Logger log = LoggerFactory.getLogger(PandemicApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(PandemicApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}
	

	@Bean
	public CommandLineRunner pandemicApp(RestTemplate restTemplate, CountryDataRepository countryDataRepository, TimelineRepository timelineRepository) throws Exception {
		return args -> { 
			
			//fetch today's data of all countries through "NOVEL COVID API" (https://corona.lmao.ninja)
			//Data source: "Worldometers".
			ResponseEntity<CountryData[]> response = restTemplate.getForEntity(
					"https://corona.lmao.ninja/v2/countries", CountryData[].class);
			CountryData[] dataList = response.getBody();
					
			for (CountryData countryData : dataList) {
				
				String country = countryData.getCountry();
				String continent = countryData.getContinent();
				Integer cases = countryData.getCases();
				Integer deaths = countryData.getDeaths();
				Integer critical = countryData.getCritical();
				Integer recovered = countryData.getRecovered();
				Integer active = countryData.getActive();
				Integer tests = countryData.getTests();
				CountryInfo countryInfo = countryData.getCountryInfo();
				Date date = countryData.getDate();
				
				log.info(countryData.toString());	
				
				//save to database
				countryDataRepository.save(new CountryData(country, continent, countryInfo, cases, deaths, critical, recovered, active, tests, date));				
			
			}
			
			//fetch global timeline from "ABOUT-CORONA.NET" (https://about-corona.net)
			//Data source: "Johns Hopkins CSSE"
			TimelineWrapper response1 = restTemplate.getForObject("https://corona-api.com/timeline", TimelineWrapper.class); 
			List<Timeline> timelineList = response1.getData();
			
			for(Timeline timeline : timelineList) {
				
				Integer confirmed = timeline.getConfirmed();
				Integer deaths = timeline.getDeaths();
				Integer recovered = timeline.getRecovered();
				Integer active = timeline.getActive();
				Date date = timeline.getDate();
				
				log.info(timeline.toString());
			
				//save to database
				timelineRepository.save(new Timeline(confirmed, deaths, recovered, active, date));			
			}
			
				
			//fetch country timeline from "ABOUT-CORONA.NET" (https://about-corona.net)
			//This is for searching country, not for save to database, will go controller part, need user to feed the ISO-2 code.
			//e.g. https://corona-api.com/countries/{FR}, see CountryInfo
			CountryTimelineWrapper response2 = restTemplate.getForObject("https://corona-api.com/countries/FR", CountryTimelineWrapper.class); 
			CountryTimeline value = response2.getData();
			String country = value.getName();
			Integer population = value.getPopulation();
			CountryLatest latestData = value.getLatest_data();
			log.info("[Country Name:] " + country + " [Population:] " + population);
			log.info(latestData.toString());
			
			List<Timeline> timelines = value.getTimeline();				
			
			for(Timeline timeline: timelines) {
				
//				Integer confirmed = timeline.getConfirmed();
//				Integer deaths = timeline.getDeaths();
//				Integer recovered = timeline.getRecovered();
//				Integer active = timeline.getActive();
//				Date date = timeline.getDate();
			
				log.info(timeline.toString());
				
			}
												
		};
	}
}
