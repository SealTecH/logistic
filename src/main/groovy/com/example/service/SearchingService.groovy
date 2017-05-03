package com.example.service

import com.example.model.FindQueryModel
import org.elasticsearch.action.bulk.byscroll.BulkByScrollResponse
import org.elasticsearch.action.search.SearchRequestBuilder
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.index.query.Operator
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.reindex.DeleteByQueryAction
import org.elasticsearch.search.sort.SortBuilders
import org.elasticsearch.search.sort.SortOrder
import org.elasticsearch.transport.client.PreBuiltTransportClient
import org.springframework.stereotype.Service
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.action.search.SearchResponse

import javax.annotation.PreDestroy
import java.sql.Timestamp

@Service
class SearchingService {

    private TransportClient client;
    private SearchRequestBuilder request;


    SearchingService() {
        this.OpenConnect();
        this.request = this.client.prepareSearch("torrents").setSearchType(SearchType.DFS_QUERY_THEN_FETCH).setIndices();
    }

    @PreDestroy
    public void springPreDestroy() {
        this.client.close();
    }

    public SearchResponse Search(String searchString, Integer page, Integer sort, ArrayList<String> types) {
        SearchRequestBuilder request;
        if (types != null) {
            def query = QueryBuilders.boolQuery();
            types.each {
                switch (it) {
                    case "Video": query = query.filter(QueryBuilders.termQuery("type_is_video", true)); break;
                    case "Audio": query = query.filter(QueryBuilders.termQuery("type_is_audio", true)); break;
                    case "Emulated Formats": query = query.filter(QueryBuilders.termQuery("type_is_emulated", true)); break;
                    case "Archive": query = query.filter(QueryBuilders.termQuery("type_is_archive", true)); break;
                    case "Text Formats": query = query.filter(QueryBuilders.termQuery("type_is_text", true)); break;
                    case "Executable": query = query.filter(QueryBuilders.termQuery("type_is_exe", true)); break;
                    case "APK": query = query.filter(QueryBuilders.termQuery("type_is_apk", true)); break;
                }

            }
            request = this.request.setQuery(query.must(QueryBuilders.matchQuery('search', searchString).operator(Operator.AND))).setFrom(page).setSize(100);
        } else {
            request = this.request.setQuery(QueryBuilders.matchQuery('search', searchString).operator(Operator.AND)).setFrom(page).setSize(100);
        }
        if (sort != null)
            switch (sort) {
                case 2: request.addSort(SortBuilders.fieldSort("fileSize").order(SortOrder.DESC)); break;
                case 3: request.addSort(SortBuilders.fieldSort("seeders").order(SortOrder.DESC)); break;
                case 4: request.addSort(SortBuilders.fieldSort("peers_updated").order(SortOrder.DESC)); break;
            }
        println("REQUEST IS ");
        println(request);
        return request.get();
    }

    public void OpenConnect() {
        this.client = new PreBuiltTransportClient(Settings.EMPTY)
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("104.198.110.70"), 9300));
    }


    public void kill() {
        Timestamp tm = new Timestamp(System.currentTimeMillis());
        long dt = tm.getTime();
        dt = (dt / 1000) - 432000;
        println(dt);
        def request = DeleteByQueryAction.INSTANCE.newRequestBuilder(client).filter(QueryBuilders.rangeQuery("seeder_was").lt(1493305412)).source("torrents");
        println("REQUEST IS ");
        println(request);
        BulkByScrollResponse resp = request.get();
        println("delete " + resp.deleted);
    }

}

