package main.java.com.ecommerce;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


// AINDA FALTA FAZER A PARTE DE CALCULA O PRECO TOTAL DE CADA PEDIDO
// eu não tenho certeza se os pedidos pendentes estão sendo processados - necessario verificar
// Sinto que ainda tem algo errado, ele ta rejeitando muito pedido e não estou vendo os pedidos pedentes serem processados
public class EccomerceSystem {
	private static final int CAPACIDADE_DA_FILA = 100;
	private static BlockingQueue<Pedido> filaPedidos = new LinkedBlockingQueue<>(CAPACIDADE_DA_FILA);
	private static BlockingQueue<Pedido> filaPedidosPendentes = new LinkedBlockingQueue<>(CAPACIDADE_DA_FILA);
	private static List<Thread> processadores = new ArrayList<>();
	private static List<Thread> geradoresDePedidos = new ArrayList<>();
	
	public static void main(String[] args) throws InterruptedException {

        Estoque estoque = new Estoque();
        
//        MUDEM OS VALORES DA QUANTIDADE DE CLIENTES SE ACHAREM NECESSÁRIOS - DEIXEI 3 SÓ PRA EXEMPLIFICAR
//           ESSE DEU BUG: 
        //        for (int i = 0; i < 3; i++) {
//            GeradorDePedidos geradorPedidos = new GeradorDePedidos(filaPedidos);
//            Thread geradorThread = new Thread(geradorPedidos);
//            geradoresDePedidos.add(geradorThread);
//            geradorThread.start();
//        }
        
//        Esse pega, mas só esta simulando 1 cliente :(
        GeradorDePedidos geradorPedidos = new GeradorDePedidos(filaPedidos);
        
        new Thread(geradorPedidos).start();
        
//      MUDEM OS VALORES DA QUANTIDADE DE PROCESSADORES SE ACHAREM NECESSÁRIOS
        for (int i = 0; i < 5; i++) {
        	ProcessadorPedidos processador = new ProcessadorPedidos(filaPedidos, filaPedidosPendentes, estoque);
            Thread processadorThread = new Thread(processador);
            processadores.add(processadorThread);
            processadorThread.start();
        }
        
        ScheduledExecutorService reabastecedor = Executors.newScheduledThreadPool(1);
        reabastecedor.scheduleAtFixedRate(() -> {
            estoque.reabastecer();

            for (Thread processador : processadores) {
                ((ProcessadorPedidos) ((Runnable) processador)).reprocessarPedidosPendentes();
            }
        }, 10, 10, TimeUnit.SECONDS);
  
        
        ScheduledExecutorService relatorioVendas = Executors.newScheduledThreadPool(1);
        relatorioVendas.scheduleAtFixedRate(() -> Relatorio.gerarRelatorio(), 30, 30, TimeUnit.SECONDS);
        
    }
}