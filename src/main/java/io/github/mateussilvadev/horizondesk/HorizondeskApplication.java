package io.github.mateussilvadev.horizondesk;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HorizondeskApplication {

	public static void main(String[] args) {
		String profile = System.getProperty("spring.profiles.active");
		if (profile == null || profile.isBlank())
			profile = "dev";

		String envFilename = profile.equals("prod") ? ".env.prod" : ".env.local";

		try {
            Dotenv dotenv = Dotenv.configure()
					.filename(envFilename)
					.ignoreIfMalformed()
					.ignoreIfMissing()
					.load();

			dotenv.entries().forEach(entry ->
					System.setProperty(entry.getKey(), entry.getValue()));
		} catch (Exception e) {
			System.out.println("Aviso: Arquivo " + envFilename + " não foi carregado. Usando variáveis do sistema.");
		}

		SpringApplication.run(HorizondeskApplication.class, args);
	}

}
