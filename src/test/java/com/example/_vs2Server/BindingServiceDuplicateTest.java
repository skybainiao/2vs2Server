package com.example._vs2Server;

import com.example._vs2Server.dto.BindingRequest;
import com.example._vs2Server.dto.SourceData;
import com.example._vs2Server.model.Binding;
import com.example._vs2Server.repository.BindingRepository;
import com.example._vs2Server.service.BindingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Transactional
public class BindingServiceDuplicateTest {

    @Autowired
    private BindingService bindingService;

    @Autowired
    private BindingRepository bindingRepository;

    @BeforeEach
    @Rollback(false)
    public void setUp() {
        // 清空数据库
        bindingRepository.deleteAll();

        // 插入两条初始数据
        List<BindingRequest> initialRequests = createInitialRequests();
        bindingService.saveBindings(initialRequests);
    }
    @Rollback(false)
    private List<BindingRequest> createInitialRequests() {
        List<BindingRequest> requests = new ArrayList<>();

        // 第一条初始请求
        BindingRequest request1 = new BindingRequest();
        SourceData source1_1 = new SourceData();
        source1_1.setLeagueName("1");
        source1_1.setHomeTeam("a1");
        source1_1.setAwayTeam("b1");
        source1_1.setSource(1);

        SourceData source2_1 = new SourceData();
        source2_1.setLeagueName("2");
        source2_1.setHomeTeam("a2");
        source2_1.setAwayTeam("b2");
        source2_1.setSource(2);

        SourceData source3_1 = new SourceData();
        source3_1.setLeagueName("3");
        source3_1.setHomeTeam("a3");
        source3_1.setAwayTeam("b3");
        source3_1.setSource(3);

        request1.setSource1(source1_1);
        request1.setSource2(source2_1);
        request1.setSource3(source3_1);


        requests.add(request1);

        return requests;
    }

    @Test
    @Rollback(false)
    public void testSaveDuplicateBindingsAfterInitialInsert() {
        // 创建包含重复数据的请求列表
        List<BindingRequest> duplicateRequests = createDuplicateRequests();

        // 保存重复数据
        bindingService.saveBindings(duplicateRequests);

        // 验证保存的数据
        List<Binding> savedBindings = bindingRepository.findAll();
        assertEquals(2, savedBindings.size());

        // 验证新插入的数据
        Binding newBinding = savedBindings.get(1);
        assertNotNull(newBinding);


    }


    @Rollback(false)
    private List<BindingRequest> createDuplicateRequests() {
        List<BindingRequest> requests = new ArrayList<>();

        // 第一条包含重复数据的请求
        BindingRequest request1 = new BindingRequest();
        SourceData source1_1 = new SourceData();
        source1_1.setLeagueName("Argentina -   Pro");
        source1_1.setHomeTeam("Deportes Iquique");
        source1_1.setAwayTeam("Deportes Iquique");
        source1_1.setSource(1);

        SourceData source2_1 = new SourceData();
        source2_1.setLeagueName("Argentina Liga  ");
        source2_1.setHomeTeam("Deportes Iquique");
        source2_1.setAwayTeam("Deportes Iquique");
        source2_1.setSource(2);

        SourceData source3_1 = new SourceData();
        source3_1.setLeagueName("Argentina -  ");
        source3_1.setHomeTeam("Deportes Iquique");
        source3_1.setAwayTeam("Deportes Iquique");
        source3_1.setSource(3);

        request1.setSource1(source1_1);
        request1.setSource2(source2_1);
        request1.setSource3(source3_1);



        requests.add(request1);

        return requests;
    }
}