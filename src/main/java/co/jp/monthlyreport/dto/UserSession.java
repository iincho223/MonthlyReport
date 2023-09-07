package co.jp.monthlyreport.dto;

import java.io.Serializable;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import lombok.Data;

@Data
@Component
@SessionScope
public class UserSession implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 社員番号 */
    private String syainNo;

    /** 会社コード */
    private String kaisyaCd;
    
}
