package xin.ctkqiang.mybursa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 马股通 应用程序入口 (Application Entry Point)。
 *
 * <p>本项目将原 Python 抓取脚本重构为结构化的 Spring Boot 应用，采用经典的
 * MVC 分层架构 (Model-View-Controller)：
 * <ul>
 *     <li><b>Model</b>：{@code model} 包中的 JPA 实体与 {@code repository} 持久层。</li>
 *     <li><b>View</b>：{@code templates} 中的 Thymeleaf 仪表盘页面。</li>
 *     <li><b>Controller</b>：{@code controller} 包中的 REST / 页面控制器。</li>
 * </ul>
 *
 * <p>{@link EnableScheduling} 启用定时任务，等价于原 Python 版本的 {@code schedule} 库，
 * 用于在交易时段内周期性抓取指数行情。
 *
 * @author 钟智强
 */
@SpringBootApplication
@EnableScheduling
public class MaGuTongApplication {

    /**
     * 程序主方法 (Main method)。
     *
     * @param args 命令行参数 (command-line arguments)
     */
    public static void main(String[] args) {
        SpringApplication.run(MaGuTongApplication.class, args);
    }
}
