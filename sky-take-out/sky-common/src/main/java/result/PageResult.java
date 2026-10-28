package result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<E> {
    private long total;
    private List<E> records;
    private int pageSize;
    private int pageNum;
}
