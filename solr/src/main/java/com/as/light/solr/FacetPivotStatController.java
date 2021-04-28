package com.as.light.solr;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@RestController
public class FacetPivotStatController {
    @Value("${solrUrl}")
    private String solrUrl;
    @Autowired
    private Configuration configuration;

    @GetMapping(value = { "statExport"})
    public void  getTypeTree(HttpServletResponse httpServletResponse) throws Exception {
        List dataList= new LinkedList<>();
        HttpSolrClient httpSolrClient = new HttpSolrClient(solrUrl);
        SolrQuery sQuery = new SolrQuery();
        sQuery.setFacet(true);
        sQuery.add("facet.pivot", "DATA_SOURCE,PUBLISH_TYPE");        //根据这两维度(数据来源,发表类型——比如:论坛,评论)来分组查询
        sQuery.add("rows", "0");
        sQuery.add("wt", "json");
        sQuery.add("indent", "true");
        sQuery.add("type","index");
        sQuery.add("q","SAVE_TIME:[20210402000000 TO 20210403000000] AND PUBLISH_TIME:[20210401000000 TO 20210403000000]");
        QueryResponse response = httpSolrClient.query(sQuery);
        NamedList<List<PivotField>> namedList = response.getFacetPivot();
        if(namedList != null) {
            List<PivotField> pivotList = null;
            for(int j=0;j<namedList.size();j++) {
                pivotList = namedList.getVal(j);
                if(pivotList != null) {
                    for(PivotField pivot:pivotList) {
                        String dataSource = pivot.getValue().toString();
                        List<PivotField> fieldList = pivot.getPivot();
                        if(fieldList != null){
                            for(PivotField field:fieldList){
                                String publishType = field.getValue().toString();
                                int count = field.getCount();
                                Map dpMap = new HashMap();
                                dpMap.put(dataSource + "-" + publishType, count);
                                dataList.add(dpMap);
                            }
                        }
                    }
                }
            }
        }
    }

    public void export(HttpServletResponse response,List dataList) throws Exception {
        HashMap<String, Object> map = new HashMap<>();
        map.put("dataList", dataList);
        //构造输出流
        Template template = configuration.getTemplate("stat.ftl", "UTF-8");
        String fileName = "/home/stat.xls";
        File file = new File(fileName);
        FileWriter out = new FileWriter(fileName);
        //模板数据渲染
        template.process(map, out);
        //将文件输出到response,返回给客户端
        FileInputStream in = new FileInputStream(file);
        byte[] buffer = new byte[in.available()];
        in.read(buffer);
        in.close();
        response.reset();
        response.addHeader("Content-Disposition", "attachment;filename=stat.xls");
        ServletOutputStream outputStream = response.getOutputStream();
        response.setContentType("application/octet-stream");
        outputStream.write(buffer);
        outputStream.flush();
        outputStream.close();
    }


}
