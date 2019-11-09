package dev.wolveringer.tdft.h4;

import dev.wolveringer.tdft.TestContext;
import dev.wolveringer.tdft.source.TestSource;
import dev.wolveringer.tdft.test.Test;
import dev.wolveringer.tdft.test.TestUnit;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Pizza extends TestUnit {
    public Pizza() {
        super("H2.1/H2.2 Pizza Factory");
    }

    @Override
    public boolean executable(TestSource source) {
        return source.getProjectName().toLowerCase().startsWith("h04_");
    }

    @Override
    protected void registerTests(TestContext context) {
        this.availTests();
    }

    private void ensureNoMethodsPublic(Class klass) {
        for(Method m : klass.getDeclaredMethods()) {
            if((m.getModifiers() & ~(Modifier.PRIVATE)) > 0) {
                throw new RuntimeException("Method " + m.getName() + " in class " + klass.getName() + " has more access than it should have be! (" + Modifier.toString(m.getModifiers()) + ")");
            }
        }
    }

    private Test[] availTests() {
        Test[] result = new Test[5];

        Test testInterfaceTeig = this.registerTest(context -> {
            context.getHelper().resolveClass("h4.Teig", Modifier.INTERFACE);
        }, "interface test Teig");

        Test testInterfaceSosse = this.registerTest(context -> {
            context.getHelper().resolveClass("h4.Sosse", Modifier.INTERFACE);
        }, "interface test Sosse");

        Test testInterfaceFabrik = this.registerTest(context -> {
            context.getHelper().resolveClass("h4.PizzaZutatenFabrik", Modifier.INTERFACE);
        }, "interface test PizzaZutatenFabrik");

        Test testClassPizza = this.registerTest(context -> {
            Class pizza = context.getHelper().resolveClass("h4.Pizza", Modifier.PUBLIC);
            ensureNoMethodsPublic(pizza);
        }, "class test Pizza");

        Test testImplementesTeig = this.registerTest(context -> {
            Class<?> klass = context.getHelper().resolveClass("h4.Teig");

            Class<?> klassDickerTeig = context.getHelper().resolveClass("h4.DickerTeig", 0);
            Class<?> klassDuennerTeig = context.getHelper().resolveClass("h4.DuennerTeig", 0);
            Class<?> klassGlutenfreierTeig = context.getHelper().resolveClass("h4.GlutenfreierTeig", 0);

            context.getHelper().ensureImplements(klassDickerTeig, klass);
            context.getHelper().ensureImplements(klassDuennerTeig, klass);
            context.getHelper().ensureImplements(klassGlutenfreierTeig, klass);
        }, "test Teig sub classes").requireTest(testInterfaceTeig);

        Test testImplementesSosse = this.registerTest(context -> {
            Class<?> klass = context.getHelper().resolveClass("h4.Sosse");

            Class<?> klassBioTomatenSosse = context.getHelper().resolveClass("h4.BioTomatenSosse", 0);
            Class<?> klassHausgemachteSosse = context.getHelper().resolveClass("h4.HausgemachteSosse", 0);
            Class<?> klassScharfeSosse = context.getHelper().resolveClass("h4.ScharfeSosse", 0);

            context.getHelper().ensureImplements(klassBioTomatenSosse, klass);
            context.getHelper().ensureImplements(klassHausgemachteSosse, klass);
            context.getHelper().ensureImplements(klassScharfeSosse, klass);
        }, "test Sosse sub classes").requireTest(testInterfaceSosse);

        Test testImplementesFabrik = this.registerTest(context -> {
            Class<?> klass = context.getHelper().resolveClass("h4.PizzaZutatenFabrik");

            Class<?> klassHochwertigeZutatenFabrik = context.getHelper().resolveClass("h4.HochwertigeZutatenFabrik", 0);
            Class<?> klassIndustrielleZutatenFabrik = context.getHelper().resolveClass("h4.IndustrielleZutatenFabrik", 0);
            Class<?> klassVeganeZutatenFabrik = context.getHelper().resolveClass("h4.VeganeZutatenFabrik", 0);

            context.getHelper().ensureImplements(klassHochwertigeZutatenFabrik, klass);
            context.getHelper().ensureImplements(klassIndustrielleZutatenFabrik, klass);
            context.getHelper().ensureImplements(klassVeganeZutatenFabrik, klass);
        }, "test Fabrik sub classes").requireTest(testInterfaceSosse);


        Test testClassDarmstaeterPizzeria = this.registerTest(context -> {
            Class<?> pizza = context.getHelper().resolveClass("h4.Pizza", Modifier.PUBLIC);
            Class<?> dpizza = context.getHelper().resolveClass("h4.DarmstaedterPizzeria", Modifier.PUBLIC);
            Class<?> pizzaFactory = context.getHelper().resolveClass("h4.PizzaZutatenFabrik");

            //TODO: Test for private constructor
            context.getHelper().resolveMethod(dpizza, "erstellePizza", Modifier.PUBLIC | Modifier.STATIC, pizza, pizzaFactory);
            context.getHelper().resolveMethod(dpizza, "ersetzeZutat", Modifier.PUBLIC | Modifier.STATIC, pizza, pizza, pizzaFactory, String.class);

            Class<?> klassHochwertigeZutatenFabrik = context.getHelper().resolveClass("h4.HochwertigeZutatenFabrik");
            Class<?> klassIndustrielleZutatenFabrik = context.getHelper().resolveClass("h4.IndustrielleZutatenFabrik");
            Class<?> klassVeganeZutatenFabrik = context.getHelper().resolveClass("h4.VeganeZutatenFabrik");

            Field hzf = context.getHelper().resolveField(dpizza, "HOCHWERTIGE_ZUTATEN_FABRIK", Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
            Field izf = context.getHelper().resolveField(dpizza, "INDUSTRIELLE_ZUTATEN_FABRIK", Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
            Field vzf = context.getHelper().resolveField(dpizza, "VEGANE_ZUTATEN_FABRIK", Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);

            Validate.isTrue(hzf.getGenericType() == klassHochwertigeZutatenFabrik,
                    "Generic type for HOCHWERTIGE_ZUTATEN_FABRIK does not match. (Expected: " + klassHochwertigeZutatenFabrik.getName() + ", Got: " + hzf.getGenericType().getTypeName() + ")");
            Validate.isTrue(izf.getGenericType() == klassIndustrielleZutatenFabrik,
                    "Generic type for INDUSTRIELLE_ZUTATEN_FABRIK does not match. (Expected: " + klassIndustrielleZutatenFabrik.getName() + ", Got: " + hzf.getGenericType().getTypeName() + ")");
            Validate.isTrue(vzf.getGenericType() == klassVeganeZutatenFabrik,
                    "Generic type for VEGANE_ZUTATEN_FABRIK does not match. (Expected: " + klassVeganeZutatenFabrik.getName() + ", Got: " + hzf.getGenericType().getTypeName() + ")");

            Object hzfInstance = hzf.get(null);
            Object izfInstance = izf.get(null);
            Object vzfInstance = vzf.get(null);
            Validate.notNull(hzfInstance, "HOCHWERTIGE_ZUTATEN_FABRIK should not be null");
            Validate.notNull(izfInstance, "INDUSTRIELLE_ZUTATEN_FABRIK should not be null");
            Validate.notNull(vzfInstance, "VEGANE_ZUTATEN_FABRIK should not be null");

            Validate.isTrue(hzfInstance.getClass() == klassHochwertigeZutatenFabrik, "Object assigned to HOCHWERTIGE_ZUTATEN_FABRIK does not matches the underlying " + klassHochwertigeZutatenFabrik.getName());
            Validate.isTrue(izfInstance.getClass() == klassIndustrielleZutatenFabrik, "Object assigned to INDUSTRIELLE_ZUTATEN_FABRIK does not matches the underlying " + klassIndustrielleZutatenFabrik.getName());
            Validate.isTrue(vzfInstance.getClass() == klassVeganeZutatenFabrik, "Object assigned to VEGANE_ZUTATEN_FABRIK does not matches the underlying " + klassVeganeZutatenFabrik.getName());

        }, "class test DarmstaeterPizzeria").requireTest(testImplementesFabrik);

        result[0] = testInterfaceTeig;
        result[1] = testInterfaceSosse;
        result[2] = testInterfaceFabrik;
        result[3] = testClassPizza;
        result[4] = testClassDarmstaeterPizzeria;
        return result;
    }

    private void testFunctionality(Test[] req) {
        //TODO: Test if every pizza gets created corretctly
        //TODO: Test for ersetzeZutat
    }
}
