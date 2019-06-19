import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final int maximo = 100;
    private static DAO dao;
    private static ArrayBlockingQueue<Entidade> InsertQueue;
    private static ArrayBlockingQueue<Entidade> UpdateQueue;
    private static ArrayBlockingQueue<Integer> DeleteQueue;

    public static void main(String[] args) {
        InsertQueue = new ArrayBlockingQueue<Entidade>(50);
        UpdateQueue = new ArrayBlockingQueue<Entidade>(50);
        DeleteQueue = new ArrayBlockingQueue<Integer>(1);

        dao = new DAO(maximo);
        executar();

    }

    public static void executar() {
        int k = 1;
        final long tempo = System.currentTimeMillis();
        final int j = k;
        for (k = 1; k <= maximo; k++) {
            final int i = k;

            final Entidade entidade = new Entidade();
            entidade.setId(k);
            entidade.setNome("NOME " + i);
            entidade.setDelete(false);
            entidade.setUpdate(false);

            //INSERINDO NO BANCO DE DADOS
            Runnable inserir = new Runnable() {
                public void run() {
                    try {
                        System.out.println("Inserindo " + i);
                        //
                        dao.salvar(entidade);
                        InsertQueue.put(entidade);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };

            //ATUALIZANDO O BANCO DE DADOS
            Runnable atualizar = new Runnable() {
                public void run() {
                    try {
                        Entidade entidade = InsertQueue.take();
                        System.out.println("Atualizando " + i);
                        entidade.setUpdate(true);
                        dao.atualizar(entidade);
                        UpdateQueue.put(entidade);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            };

            //DELETANDO DADOS DO BANDO DE DADOS
            Runnable deletar = new Runnable() {
                public void run() {
                    try {
                        Entidade entidade = UpdateQueue.take();

                        System.out.println("Deletando " + i);
                        entidade.setDelete(true);
                        dao.atualizar(entidade);
                        if (i >= 1000) {
                            long tempo2 = System.currentTimeMillis() - tempo;
                            System.out.println("=====> Tempo execuçao: " + tempo2);
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };

            //THREAD
            Thread tInserir = new Thread(inserir);
            Thread tAtualizar = new Thread(atualizar);
            Thread tDeletar = new Thread(deletar);

            tInserir.start();
            tAtualizar.start();
            tDeletar.start();

        }
        opcoesUsuario();
    }

    public static void opcoesUsuario() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("DIGITE 1 PARA REINICIAR O PROCESSO OU 2 PARA CANCELAR A OPERAÇAO");
        int opcao = scanner.nextInt();
        switch (opcao) {
            case 1: {
                System.out.println("Reiniciando processo");
                dao.cancelar();
                System.out.println("Executando o processo novamente");
                executar();
                break;
            }
            case 2: {
                System.out.println("Cancelando a operaçao");
                dao.cancelar();
                break;
            }
            default: {
                System.exit(0);
            }
        }
    }
}
