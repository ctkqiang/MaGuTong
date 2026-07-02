package com.zhongzhiqiang.magutong.controller;

import com.zhongzhiqiang.magutong.model.IndexQuote;
import com.zhongzhiqiang.magutong.repository.IndexQuoteRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 指数行情查询控制器 (Index quote query controller)。
 *
 * <p>提供只读的行情查询接口，供前端或第三方系统消费。
 *
 * @author 钟智强
 */
@RestController
@RequestMapping("/api/quotes")
public class IndexQuoteController {

    private final IndexQuoteRepository repository;

    public IndexQuoteController(IndexQuoteRepository repository) {
        this.repository = repository;
    }

    /**
     * 查询全部行情 (List all quotes)。
     */
    @GetMapping
    public ResponseEntity<List<IndexQuote>> listAll() {
        return ResponseEntity.ok(repository.findAll());
    }

    /**
     * 按行情代码查询单条 (Get a single quote by ticker id)。
     *
     * @param tickerId 行情代码，如 "5099.KL"
     */
    @GetMapping("/{tickerId}")
    public ResponseEntity<IndexQuote> getOne(@PathVariable String tickerId) {
        return repository.findById(tickerId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
