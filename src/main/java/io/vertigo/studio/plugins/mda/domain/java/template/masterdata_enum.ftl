package ${entity.packageName};

import java.io.Serializable;

import io.vertigo.dynamo.domain.model.MasterDataEnum;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

public enum ${entity.classSimpleName}Enum implements MasterDataEnum<${entity.className}> {

<#list entity.enumValues as enumValue>
	${enumValue.name}("${enumValue.id}")<#sep>, //</#sep>
</#list>	;

	private final Serializable entityId;

	private ${entity.classSimpleName}Enum(final Serializable id) {
		entityId = id;
	}

	@Override
	public URI<${entity.className}> getEntityUri() {
		return DtObjectUtil.createURI(${entity.className}.class, entityId);
	}

}
