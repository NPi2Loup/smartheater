package com.mlaroche.smartheater.mda;

import javax.inject.Inject;

import io.vertigo.app.AutoCloseableApp;
import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.commons.CommonsFeatures;
import io.vertigo.core.component.di.injector.DIInjector;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.dynamo.DynamoFeatures;
import io.vertigo.dynamo.plugins.environment.DynamoDefinitionProvider;
import io.vertigo.studio.StudioFeatures;
import io.vertigo.studio.mda.MdaManager;

public class Studio {

	private static AppConfig buildAppConfig() {
		// @formatter:off
		return  AppConfig.builder()
				.beginBoot()
				.withLocales("fr_FR")
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.endBoot()
				.addModule(new CommonsFeatures()
						.withCache()
						.withMemoryCache()
						.withScript()
						.withJaninoScript()
						.build())
				.addModule(new DynamoFeatures().build())
				//----Definitions
				.addModule(ModuleConfig.builder("ressources")
						.addDefinitionProvider(DefinitionProviderConfig.builder(DynamoDefinitionProvider.class)
								.addParam(Param.of("encoding", "UTF-8"))
								.addDefinitionResource("kpr", "com/mlaroche/smartheater/domain/generation.kpr")
								.build())
						.build())
				// ---StudioFeature
				.addModule( new StudioFeatures()
					.withMasterData()
					.withMda(Param.of("projectPackageName", "com.mlaroche.smartheater"))
					.withJavaDomainGenerator(
							Param.of("generateDtResources", "false"))
					.withTaskGenerator()
					.withFileGenerator()
					.withSqlDomainGenerator(
							Param.of("targetSubDir", "javagen/sqlgen"),
							Param.of("baseCible", "H2"),
							Param.of("generateDrop", "false"),
							Param.of("generateMasterData", "true"))
					.withJsonMasterDataValuesProvider(
							Param.of("fileName" ,"com/mlaroche/smartheater/domain/data/JsonMasterDataValues.json"))
					.build())
				.build();
		// @formatter:on

	}

	@Inject
	private MdaManager mdaManager;

	public static void main(final String[] args) {
		try (final AutoCloseableApp app = new AutoCloseableApp(buildAppConfig())) {
			final Studio sample = new Studio();
			DIInjector.injectMembers(sample, app.getComponentSpace());
			//-----
			sample.cleanGenerate();
		}
	}

	void cleanGenerate() {
		mdaManager.clean();
		mdaManager.generate().displayResultMessage(System.out);
	}
}
