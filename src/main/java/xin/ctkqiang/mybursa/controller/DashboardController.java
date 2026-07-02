package xin.ctkqiang.mybursa.controller;

import xin.ctkqiang.mybursa.repository.IndexQuoteRepository;
import xin.ctkqiang.mybursa.service.TradingCalendarService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 仪表盘页面控制器 (Dashboard page controller)。
 *
 * <p>MVC 中连接 "V" (Thymeleaf 模板) 的控制器。渲染一个简单的行情监控页面，
 * 展示交易状态与最新抓取的行情列表。
 *
 * @author 钟智强
 */
@Controller
public class DashboardController {

    private final IndexQuoteRepository quoteRepository;
    private final TradingCalendarService tradingCalendarService;

    public DashboardController(IndexQuoteRepository quoteRepository,
                              TradingCalendarService tradingCalendarService) {
        this.quoteRepository = quoteRepository;
        this.tradingCalendarService = tradingCalendarService;
    }

    /**
     * 渲染仪表盘首页 (Render the dashboard home page)。
     *
     * @param model 视图模型 (view model)
     * @return 模板名称 "dashboard"
     */
    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("tradingOpen", tradingCalendarService.isTradingTime());
        model.addAttribute("quotes", quoteRepository.findAll());
        return "dashboard";
    }
}
