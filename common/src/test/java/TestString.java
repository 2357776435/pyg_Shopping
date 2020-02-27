import org.junit.Test;

public class TestString {
    @Test
    public void test(){
        String conf = "classpath:fastDFS/fdfs_client.conf";
        if (conf.contains("classpath:")) {
            conf = conf.replace("classpath:", this.getClass().getResource("/").getPath());
        }
        conf = conf.replaceFirst("/", "");
        conf = conf.replaceAll("%20"," ");
        //D:/Text%20Work/IDEA/WorkSpace/pyg_parent/common/target/test-classes/fastDFS/fdfs_client.conf
        //D:/Text Work/IDEA/WorkSpace/pyg_parent/common/target/test-classes/fastDFS/fdfs_client.conf
        //D:\\Text Work\\IDEA\\WorkSpace\\pyg_parent\\web_shop\\src\\main\\resources\\fastDFS\\fdfs_client.conf
        System.out.println(conf);
    }
}
