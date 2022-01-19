import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { ICadastroTrilha, CadastroTrilha } from '../cadastro-trilha.model';

import { CadastroTrilhaService } from './cadastro-trilha.service';

describe('CadastroTrilha Service', () => {
  let service: CadastroTrilhaService;
  let httpMock: HttpTestingController;
  let elemDefault: ICadastroTrilha;
  let expectedResult: ICadastroTrilha | ICadastroTrilha[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    expectedResult = null;
    service = TestBed.inject(CadastroTrilhaService);
    httpMock = TestBed.inject(HttpTestingController);

    elemDefault = {
      id: 0,
      dataHora: 'AAAAAAA',
    };
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = Object.assign({}, elemDefault);

      service.find(123).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(elemDefault);
    });

    it('should create a CadastroTrilha', () => {
      const returnedFromService = Object.assign(
        {
          id: 0,
        },
        elemDefault
      );

      const expected = Object.assign({}, returnedFromService);

      service.create(new CadastroTrilha()).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a CadastroTrilha', () => {
      const returnedFromService = Object.assign(
        {
          id: 1,
          dataHora: 'BBBBBB',
        },
        elemDefault
      );

      const expected = Object.assign({}, returnedFromService);

      service.update(expected).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a CadastroTrilha', () => {
      const patchObject = Object.assign({}, new CadastroTrilha());

      const returnedFromService = Object.assign(patchObject, elemDefault);

      const expected = Object.assign({}, returnedFromService);

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of CadastroTrilha', () => {
      const returnedFromService = Object.assign(
        {
          id: 1,
          dataHora: 'BBBBBB',
        },
        elemDefault
      );

      const expected = Object.assign({}, returnedFromService);

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toContainEqual(expected);
    });

    it('should delete a CadastroTrilha', () => {
      service.delete(123).subscribe(resp => (expectedResult = resp.ok));

      const req = httpMock.expectOne({ method: 'DELETE' });
      req.flush({ status: 200 });
      expect(expectedResult);
    });

    describe('addCadastroTrilhaToCollectionIfMissing', () => {
      it('should add a CadastroTrilha to an empty array', () => {
        const cadastroTrilha: ICadastroTrilha = { id: 123 };
        expectedResult = service.addCadastroTrilhaToCollectionIfMissing([], cadastroTrilha);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(cadastroTrilha);
      });

      it('should not add a CadastroTrilha to an array that contains it', () => {
        const cadastroTrilha: ICadastroTrilha = { id: 123 };
        const cadastroTrilhaCollection: ICadastroTrilha[] = [
          {
            ...cadastroTrilha,
          },
          { id: 456 },
        ];
        expectedResult = service.addCadastroTrilhaToCollectionIfMissing(cadastroTrilhaCollection, cadastroTrilha);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a CadastroTrilha to an array that doesn't contain it", () => {
        const cadastroTrilha: ICadastroTrilha = { id: 123 };
        const cadastroTrilhaCollection: ICadastroTrilha[] = [{ id: 456 }];
        expectedResult = service.addCadastroTrilhaToCollectionIfMissing(cadastroTrilhaCollection, cadastroTrilha);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(cadastroTrilha);
      });

      it('should add only unique CadastroTrilha to an array', () => {
        const cadastroTrilhaArray: ICadastroTrilha[] = [{ id: 123 }, { id: 456 }, { id: 67937 }];
        const cadastroTrilhaCollection: ICadastroTrilha[] = [{ id: 123 }];
        expectedResult = service.addCadastroTrilhaToCollectionIfMissing(cadastroTrilhaCollection, ...cadastroTrilhaArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const cadastroTrilha: ICadastroTrilha = { id: 123 };
        const cadastroTrilha2: ICadastroTrilha = { id: 456 };
        expectedResult = service.addCadastroTrilhaToCollectionIfMissing([], cadastroTrilha, cadastroTrilha2);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(cadastroTrilha);
        expect(expectedResult).toContain(cadastroTrilha2);
      });

      it('should accept null and undefined values', () => {
        const cadastroTrilha: ICadastroTrilha = { id: 123 };
        expectedResult = service.addCadastroTrilhaToCollectionIfMissing([], null, cadastroTrilha, undefined);
        expect(expectedResult).toHaveLength(1);
        expect(expectedResult).toContain(cadastroTrilha);
      });

      it('should return initial array if no CadastroTrilha is added', () => {
        const cadastroTrilhaCollection: ICadastroTrilha[] = [{ id: 123 }];
        expectedResult = service.addCadastroTrilhaToCollectionIfMissing(cadastroTrilhaCollection, undefined, null);
        expect(expectedResult).toEqual(cadastroTrilhaCollection);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});