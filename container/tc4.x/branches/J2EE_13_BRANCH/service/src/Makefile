all: Makedefs
	make -C native all
	make -C share all
	make -C share javadocs 
	cp native/jsvc ../jsvc
	cp service.jar ../service.jar

clean: Makedefs
	make -C native clean
	make -C share clean

distclean: clean
	rm -f Makedefs
	rm -rf ../javadocs
	rm -f ../jsvc
	rm -f ../service.jar

Makedefs: Makedefs.orig
	@echo "### WARNING: Edit Makedefs before issuing make ###"
	@cp Makedefs.orig Makedefs
	@exit 1
