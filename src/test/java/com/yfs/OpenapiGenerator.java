package com.yfs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.yfs.util.Ftls;

public class OpenapiGenerator {
	public static void main(String[] args) throws IOException {
		String name = "saleVolumeOneQuery";
		String comment = "电商查询sap实时可销售量   按单个物料+库位查询";
		String className = "SIEC2ECCSingleSalesQuerySynOut";
		String url = "dir/wsdl?p=1_e157cd93e6093ffbbe2c2b65da099ef2_portTypeBindingService";

		Map<String, Object> root = new HashMap<>();
		String baseWebDirPath = "D:\\workspace\\chenem-git\\chemcn-ec-openapi-web\\src\\main\\java\\com\\chemcn\\ec\\web\\newOpenapi\\webServiceClient\\product\\yq";
		createDir(Paths.get(baseWebDirPath, name));
		String baseApiDirPath = "D:\\workspace\\chenem-git\\chemcn-ec-center-openapi\\chemcn-ec-service-openapi\\chemcn-ec-openapi-api\\src\\main\\java\\com\\chemcn\\ec\\servicecenter\\openapi\\webServiceClient\\api\\product\\domain\\yq";
		createDir(Paths.get(baseApiDirPath, name));

		root.put("name", name);
		root.put("comment", comment);
		root.put("className", className);
		Ftls.processFileTemplateToConsole("opeanapi-client.ftl", root);

		System.out.println("\n\n\nsap.yq." + name + ".url=http://10.8.10.90:50000/" + url);
	}

	private static void createDir(Path path) throws IOException {
		if (!Files.exists(path)) {
			Files.createDirectories(path);
		}
	}
}
