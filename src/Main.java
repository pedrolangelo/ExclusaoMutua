import java.util.ArrayList;
import java.util.Random;

public class Main 
{
    private static final int ADICIONA = 4000;
    private static final int INATIVO_PROCESSO = 8000;
    private static final int INATIVO_COORDENADOR = 30000;
    private static final int CONSOME_RECURSO_MIN = 5000;
    private static final int CONSOME_RECURSO_MAX = 10000; 

    int i = 0;
    static ArrayList<Integer> totalProcessos = new ArrayList<Integer>();
    static ArrayList<Integer> qtdProcessosExecutados = new ArrayList<Integer>();

    private static final Object lock = new Object();

    public static void main(String[] args) {
        criarProcessos(ControladorDeProcessos.getProcessosAtivos());
        inativarCoordenador(ControladorDeProcessos.getProcessosAtivos());
        inativarProcesso(ControladorDeProcessos.getProcessosAtivos());
        acessarRecurso(ControladorDeProcessos.getProcessosAtivos());
    }

    public static void criarProcessos(ArrayList<Processo> processosAtivos){
        new Thread (new Runnable() {
            @Override
            public void run(){
                while(true){
                    synchronized (lock){
                        Processo processo = new Processo(gerarIdUnico(processosAtivos));

                        if(processosAtivos.isEmpty()) processo.setEhCoordenador(true);

                        processosAtivos.add(processo);
                        totalProcessos.add(processo.getPid());
                        qtdProcessosExecutados.add(0);
                        System.out.println("CRIAR MATRIZ" + totalProcessos);

                    }

                    esperar(ADICIONA);
                }
            }
        }).start();
    }

    private static int gerarIdUnico(ArrayList<Processo> processosAtivos){
        Random random = new Random();

        int idRandom = random.nextInt(1000);

        for(Processo p : processosAtivos){
            if(p.getPid() == idRandom) {

                return gerarIdUnico(processosAtivos);
            }
        }

        System.out.println("Processo Ativos " + ControladorDeProcessos.getProcessosAtivos() + " Id Unico.");
        return idRandom;
    }

    public static void inativarProcesso(ArrayList<Processo> processosAtivos){
        new Thread(new Runnable() {
            public void run(){
                while(true) {
                    esperar(INATIVO_PROCESSO);

                    synchronized (lock) {
                        if(!processosAtivos.isEmpty()) {
                            int indexProcessoAleatorio = new Random().nextInt(processosAtivos.size());
                            Processo pRemover = processosAtivos.get(indexProcessoAleatorio);
                            if(pRemover != null && !pRemover.isCoordenador()) pRemover.destruir();
                        }
                    }
                }
            }
        }).start();
        System.out.println("Processo Ativos " + ControladorDeProcessos.getProcessosAtivos() + " Desativar.");

    }

    public static void inativarCoordenador(ArrayList<Processo> processosAtivos){
        new Thread (new Runnable(){
            @Override
            public void run(){
                while(true){
                    esperar(INATIVO_COORDENADOR);

                    synchronized (lock){
                        Processo coordenador = null;
                        for(Processo p : processosAtivos){
                            if(p.isCoordenador()) coordenador = p;
                        }

                        if(coordenador != null){
                            coordenador.destruir();
                            System.out.println("Processo coordenador " + coordenador + " destruído.");
                        }
                    }
                }
            }
        }).start();
    }

    public static void acessarRecurso(ArrayList<Processo> processosAtivos){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Random random = new Random();
                int intervalo = 0;
                while(true){
                    intervalo = random.nextInt(CONSOME_RECURSO_MAX - CONSOME_RECURSO_MIN);
                    esperar(CONSOME_RECURSO_MIN + intervalo);

                    synchronized(lock) {
                        if(!processosAtivos.isEmpty()){
                            int indexProcessoAleatorio = new Random().nextInt(processosAtivos.size());

                            Processo processoConsumidor = processosAtivos.get(indexProcessoAleatorio);
                            processoConsumidor.acessarRecursoCompartilhado();

                            int aux = totalProcessos.indexOf(processoConsumidor.getPid());

                            int qtd = qtdProcessosExecutados.get(aux) + 1;

                            qtdProcessosExecutados.set(aux, qtd);
                            
                            System.out.println("TOTAL AAAAAAAAAAA " + totalProcessos);
                            System.out.println("AUX AAAAAAAAAAA " + qtdProcessosExecutados);



                        }
                    }
                }
            }
        }).start();
    }

    private static void esperar(int segundos){
        try{
            Thread.sleep(segundos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}